/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.identity.keycloak;

import org.junit.Before;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

public class KeycloakEnabledBaseTestIT {

    @Value("${keycloak.auth-server-url}")
    protected String authServer;

    @Value("${keycloak.realm}")
    protected String realm;

    @Value("${keycloak.resource}")
    protected String resource;

    @Value("${keycloaktestuser}")
    protected String keycloaktestuser;

    @Value("${keycloaktestpassword}")
    protected String keycloaktestpassword;

    protected AccessTokenResponse accessToken;

    @Before
    public void setUp() throws Exception {
        accessToken = authenticateUser();
    }

    protected AccessTokenResponse authenticateUser() throws IOException {
        return Keycloak.getInstance(authServer,realm,keycloaktestuser,keycloaktestpassword,resource).tokenManager().getAccessToken();
    }

    protected HttpHeaders getHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + new String(token));
        return headers;
    }

    protected HttpEntity getRequestEntityWithHeaders(){
        return new org.springframework.http.HttpEntity(getHeaders(accessToken.getToken()));
    }
}
