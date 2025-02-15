/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.dvauth.resources;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.dans.dvauth.DdDataverseAuthenticatorApplication;
import nl.knaw.dans.dvauth.DdDataverseAuthenticatorConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(DropwizardExtensionsSupport.class)
class AuthCheckResourceIntegrationTest {

    private final DropwizardAppExtension<DdDataverseAuthenticatorConfiguration> EXT = new DropwizardAppExtension<>(
        DdDataverseAuthenticatorApplication.class,
        ResourceHelpers.resourceFilePath("test-etc/config.yml")
    );

    String generateBasicAuthHeader(String username, String password) {
        var formatted = String.format("%s:%s", username, password);
        var pass = Base64.getEncoder().encodeToString(formatted.getBytes());

        return "Basic " + pass;
    }

    @Test
    void testWithUsername() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());
        var auth = generateBasicAuthHeader("user001", "user001");

        try (var result = EXT.client()
            .target(url)
            .request()
            .header("authorization", auth)
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(204, result.getStatus());
        }
    }

    @Test
    void testWithUsernameButWrongPassword() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());
        var auth = generateBasicAuthHeader("user001", "user002");

        try (var result = EXT.client()
            .target(url)
            .request()
            .header("authorization", auth)
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(401, result.getStatus());
            assertNotNull(result.getHeaderString("WWW-Authenticate"));
        }
    }

    @Test
    void testWithUsernameButWrongUsername() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());
        var auth = generateBasicAuthHeader("non-existing", "user002");

        try (var result = EXT.client()
            .target(url)
            .request()
            .header("authorization", auth)
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(401, result.getStatus());
            assertNotNull(result.getHeaderString("WWW-Authenticate"));
        }
    }

    @Test
    void testWithoutCredentials() {
        var url = String.format("http://localhost:%s/", EXT.getLocalPort());

        try (var result = EXT.client()
            .target(url)
            .request()
            .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE))) {

            assertEquals(401, result.getStatus());
            assertNotNull(result.getHeaderString("WWW-Authenticate"));
        }
    }
}