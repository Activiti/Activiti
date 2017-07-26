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

package org.activiti.services.history;

import org.activiti.services.history.events.ProcessEngineEventEntity;
import org.activiti.services.core.model.events.ProcessEngineEvent;
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
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-producer-test.properties")
@DirtiesContext
public class HistoryServiceTests {

    private static final String relativeQueryEndpoint = "/audit/events";

    @ClassRule
    public static RabbitTestSupport rabbitTestSupport = new RabbitTestSupport();

    @Autowired
    private TestRestTemplate restTemplate;

//    @Autowired
//    private MessageCollector messageCollector;

//
//    @Autowired
//    private MessageChannel historyProducer;

    @Autowired
    MyProducer producer;

    @Test
    public void getAllEventsTests() throws Exception {

        //given
        ResponseEntity<PagedResources<ProcessEngineEventEntity>> eventsPagedResources = restTemplate.exchange(relativeQueryEndpoint + "?pageable={pageable}&size={size}",
                                                                                                              HttpMethod.GET,
                                                                                                              null,
                                                                                                              new ParameterizedTypeReference<PagedResources<ProcessEngineEventEntity>>() {
                                                                                                              }, 0,2);
        //then
        assertThat(eventsPagedResources).isNotNull();
        assertThat(eventsPagedResources.getBody().getContent()).hasSize(0);

        //given
      //  historyProducer.send(MessageBuilder.withPayload(newEvent).build());

        producer.send(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                 "ActivityStartedEvent",
                                                 "2",
                                                 "3",
                                                 "4"));

//        Message<ProcessEngineEvent> received = (Message<ProcessEngineEvent>) messageCollector.forChannel(historyProducer).poll();
//
//        //then
//        assertThat(received).isNotNull();

        //  assertThat(received.getPayload()).isEqualTo(newEvent);


        //given
        eventsPagedResources = restTemplate.exchange(relativeQueryEndpoint + "?pageable={pageable}&size={size}",
                                                     HttpMethod.GET,
                                                     null,
                                                     new ParameterizedTypeReference<PagedResources<ProcessEngineEventEntity>>() {
                                                     }, 0, 2);
        //then
        assertThat(eventsPagedResources.getBody().getContent()).hasSize(1);
        assertThat(eventsPagedResources).isNotNull();
    }

}
