

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

import java.util.HashMap;
import java.util.Map;

import org.activiti.cmdendpoint.cmds.StartProcessInstanceCmd;
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
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext
@EnableBinding(MessageClientStream.class)
public class CommandEndpointTest {

    private static final String relativeMessagesEndpoint = "/api/messages";

    @ClassRule
    public static RabbitTestSupport rabbitTestSupport = new RabbitTestSupport();

    @Autowired
    private MessageChannel myCmdProducer;

    @Autowired
    private SubscribableChannel myCmdResults;

    @Autowired
    private TestRestTemplate restTemplate;

    public static final String PROCESS_DEFINITIONS_URL = "/process-definitions/";
    public static final String PROCESS_INSTANCES_RELATIVE_URL = "/process-instances/";

    @Test
    public void getAllMessagesTests() throws Exception {

        ParameterizedTypeReference<PagedResources<ProcessDefinition>> responseType = new ParameterizedTypeReference<PagedResources<ProcessDefinition>>() {
        };
        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitionsResources = restTemplate.exchange(PROCESS_DEFINITIONS_URL,
                                                                                                              HttpMethod.GET,
                                                                                                              null,
                                                                                                              responseType);

        assertThat(processDefinitionsResources).isNotNull();
        assertThat(processDefinitionsResources.getBody()).isNotNull();
        assertThat(processDefinitionsResources.getBody().getContent()).isNotEmpty();
        ProcessDefinition aProcessDefinition = processDefinitionsResources.getBody().getContent().iterator().next();

        Map<String, String> vars = new HashMap<>();
        vars.put("hey",
                 "one");

        StartProcessInstanceCmd startProcessInstanceCmd = new StartProcessInstanceCmd(aProcessDefinition.getId(),
                                                                                      vars);
        //given
        myCmdProducer.send(MessageBuilder.withPayload(startProcessInstanceCmd).build());

        Thread.sleep(500);

        //when
        ResponseEntity<PagedResources<ProcessInstance>> processInstancesPage = restTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + "?page={page}&size={size}",
                                                                                                     HttpMethod.GET,
                                                                                                     null,
                                                                                                     new ParameterizedTypeReference<PagedResources<ProcessInstance>>() {
                                                                                                     },
                                                                                                     "0",
                                                                                                     "2");

        //then
        assertThat(processInstancesPage).isNotNull();
        assertThat(processInstancesPage.getBody().getContent()).hasSize(1);
        assertThat(processInstancesPage.getBody().getMetadata().getTotalPages()).isGreaterThanOrEqualTo(1);
    }
}
