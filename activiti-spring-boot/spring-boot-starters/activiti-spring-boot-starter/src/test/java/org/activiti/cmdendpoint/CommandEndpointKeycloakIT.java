
/*
 * Copyright 2017 Alfresco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.cmdendpoint;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.activiti.cmdendpoint.cmds.StartProcessInstanceCmd;
import org.activiti.keycloak.KeycloakEnabledBaseTestIT;
import org.activiti.services.core.model.ProcessDefinition;
import org.activiti.services.core.model.ProcessInstance;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binder.test.junit.rabbit.RabbitTestSupport;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@EnableBinding(MessageClientStream.class)
public class CommandEndpointKeycloakIT extends KeycloakEnabledBaseTestIT {

    @ClassRule
    public static RabbitTestSupport rabbitTestSupport = new RabbitTestSupport();

    @Autowired
    private MessageChannel myCmdProducer;

    @Autowired
    private TestRestTemplate restTemplate;

    public static final String PROCESS_DEFINITIONS_URL = "/v1/process-definitions/";
    public static final String PROCESS_INSTANCES_RELATIVE_URL = "/v1/process-instances/";

    @Test
    public void getAllMessagesTests() throws Exception {

        ParameterizedTypeReference<PagedResources<ProcessDefinition>> responseType = new ParameterizedTypeReference<PagedResources<ProcessDefinition>>() {
        };
        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitionsResources = restTemplate.exchange(PROCESS_DEFINITIONS_URL,
                                                                                                              HttpMethod.GET,
                                                                                                              getRequestEntityWithHeaders(),
                                                                                                              responseType);

        assertThat(processDefinitionsResources).isNotNull();
        assertThat(processDefinitionsResources.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(processDefinitionsResources.getBody()).isNotNull();
        assertThat(processDefinitionsResources.getBody().getContent()).isNotEmpty();
        ProcessDefinition aProcessDefinition = processDefinitionsResources.getBody().getContent().iterator().next();

        Map<String, String> vars = new HashMap<>();
        vars.put("hey",
                 "one");

        StartProcessInstanceCmd startProcessInstanceCmd = new StartProcessInstanceCmd(aProcessDefinition.getId(),
                                                                                      vars);

        //record what instances there were before starting this one - should be none but will check this later
        ResponseEntity<PagedResources<ProcessInstance>> processInstancesPageBefore = restTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + "?page={page}&size={size}",
                HttpMethod.GET,
                getRequestEntityWithHeaders(),
                new ParameterizedTypeReference<PagedResources<ProcessInstance>>() {
                },
                "0",
                "2");

        //given
        myCmdProducer.send(MessageBuilder.withPayload(startProcessInstanceCmd).build());

        Thread.sleep(500);

        //when
        ResponseEntity<PagedResources<ProcessInstance>> processInstancesPage = restTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + "?page={page}&size={size}",
                                                                                                     HttpMethod.GET,
                                                                                                     getRequestEntityWithHeaders(),
                                                                                                     new ParameterizedTypeReference<PagedResources<ProcessInstance>>() {
                                                                                                     },
                                                                                                     "0",
                                                                                                     "2");



        //then
        assertThat(processInstancesPage).isNotNull();
        assertThat(processInstancesPage.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(processInstancesPage.getBody().getContent().size()).isGreaterThanOrEqualTo(1);

        Collection<ProcessInstance> instances = processInstancesPage.getBody().getContent();
        for(ProcessInstance instance:instances){
            assertThat(instance.getProcessDefinitionId()).isEqualTo(aProcessDefinition.getId());
            assertThat(instance.getId()).isNotNull();
            assertThat(instance.getStartDate()).isNotNull();
            assertThat(instance.getStatus()).isEqualToIgnoringCase("RUNNING");
        }

        //should have only started one
        assertThat(processInstancesPage.getBody().getContent().size() - processInstancesPageBefore.getBody().getContent().size()).isEqualTo(1);

        //expecting we started with none
        assertThat(processInstancesPageBefore.getBody().getContent()).hasSize(0);

        assertThat(processInstancesPage.getBody().getContent()).hasSize(1);
        assertThat(processInstancesPage.getBody().getMetadata().getTotalPages()).isGreaterThanOrEqualTo(1);
    }
}
