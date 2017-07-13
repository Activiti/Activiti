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

package org.activiti.runtime;

import org.activiti.client.model.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.IOException;

import static org.activiti.runtime.ProcessInstanceRestTemplate.PROCESS_INSTANCES_RELATIVE_URL;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProcessInstanceIT {

    @Value("${keycloak.auth-server-url}")
    private String authServer;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String resource;

    @Value("${keycloaktestuser}")
    private String keycloaktestuser;

    @Value("${keycloaktestpassword}")
    private String keycloaktestpassword;

    private static final String SIMPLE_PROCESS = "SimpleProcess";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;


    protected AccessTokenResponse authenticateUser() throws IOException {
        return Keycloak.getInstance(authServer,realm,keycloaktestuser,keycloaktestpassword,resource).tokenManager().getAccessToken();
    }

    @Test
    public void shouldStartProcess() throws Exception {
        //when
        ResponseEntity<ProcessInstance> entity = processInstanceRestTemplate.startProcess(SIMPLE_PROCESS,authenticateUser());

        //then
        assertThat(entity).isNotNull();
        ProcessInstance returnedProcInst = entity.getBody();
        assertThat(returnedProcInst).isNotNull();
        assertThat(returnedProcInst.getId()).isNotNull();
        assertThat(returnedProcInst.getProcessDefinitionId()).contains("SimpleProcess:");
        assertThat(returnedProcInst.getStartUserId()).isNotNull();
        assertThat(returnedProcInst.getStartUserId()).isEqualTo(keycloaktestuser);//will only match if using username not id
    }

    @Test
    public void shouldRetrieveProcessInstanceById() throws Exception {

        AccessTokenResponse accessToken = authenticateUser();

        //given
        ResponseEntity<ProcessInstance> startedProcessEntity = processInstanceRestTemplate.startProcess(SIMPLE_PROCESS,accessToken);

        org.springframework.http.HttpEntity requestEntity = new org.springframework.http.HttpEntity(getHeaders(accessToken.getToken()));

        //when
        ResponseEntity<ProcessInstance> retrievedEntity = restTemplate.exchange(
                PROCESS_INSTANCES_RELATIVE_URL + startedProcessEntity.getBody().getId(),
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<ProcessInstance>() {
                });

        //then
        assertThat(retrievedEntity.getBody()).isNotNull();
        assertThat(retrievedEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(retrievedEntity.getBody().getId()).isNotNull();
    }

    @Test
    public void shouldRetrieveListOfProcessInstances() throws Exception {

        AccessTokenResponse accessToken = authenticateUser();

        //given
        processInstanceRestTemplate.startProcess(SIMPLE_PROCESS,accessToken);
        processInstanceRestTemplate.startProcess(SIMPLE_PROCESS,accessToken);
        processInstanceRestTemplate.startProcess(SIMPLE_PROCESS,accessToken);

        org.springframework.http.HttpEntity requestEntity = new org.springframework.http.HttpEntity(getHeaders(accessToken.getToken()));

        //when
        ResponseEntity<PagedResources<ProcessInstance>> processInstancesPage = restTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + "?page=0&size=2",
                                                                                                          HttpMethod.GET,
                                                                                                          requestEntity,
                                                                                                          new ParameterizedTypeReference<PagedResources<ProcessInstance>>() {
                                                                                                          });

        //then
        assertThat(processInstancesPage).isNotNull();
        assertThat(processInstancesPage.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(processInstancesPage.getBody().getContent()).hasSize(2);
        assertThat(processInstancesPage.getBody().getMetadata().getTotalPages()).isGreaterThanOrEqualTo(2);
    }

    private HttpHeaders getHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + new String(token));
        return headers;
    }
}