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

package org.activiti.services.identity.keycloak.interceptor;


import java.io.IOException;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class KeycloakSecurityContextClientRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";


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

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        AccessTokenResponse token = getAccessTokenResponse();
        httpRequest.getHeaders().set(AUTHORIZATION_HEADER, "Bearer " + token.getToken());
        return clientHttpRequestExecution.execute(httpRequest, bytes);
    }

    private AccessTokenResponse getAccessTokenResponse() {
        return Keycloak.getInstance(authServer, realm, keycloaktestuser, keycloaktestpassword, resource).tokenManager().getAccessToken();
    }


    public void setKeycloaktestuser(String keycloaktestuser) {
        this.keycloaktestuser = keycloaktestuser;
    }

    public String getKeycloaktestuser(){
        return keycloaktestuser;
    }
}