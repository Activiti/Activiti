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
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.junit.rabbit.RabbitTestSupport;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-producer-test.properties")
@DirtiesContext
public class HistoryServiceIT {

    private static final String relativeQueryEndpoint = "/audit/events";

    @ClassRule
    public static RabbitTestSupport rabbitTestSupport = new RabbitTestSupport();

    @Autowired
    private EventsRestTemplate eventsRestTemplate;

    @Autowired
    private EventsRepository repository;

    @Autowired
    private MyProducer producer;

    @Before
    public void setUp() throws Exception {
        repository.deleteAll();
    }

    @Test
    public void findAllShouldReturnAllAvailableEvents() throws Exception {
        //given
        producer.send(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                 "ActivityStartedEvent",
                                                 "2",
                                                 "3",
                                                 "4"));
        producer.send(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                 "ActivityCompletedEvent",
                                                 "11",
                                                 "23",
                                                 "42"));
        waitForMessage();

        //when
        ResponseEntity<PagedResources<ProcessEngineEventEntity>> eventsPagedResources = eventsRestTemplate.executeFindAll();

        //then
        assertThat(eventsPagedResources.getBody().getContent())
                .extracting(
                        ProcessEngineEventEntity::getEventType,
                        ProcessEngineEventEntity::getExecutionId,
                        ProcessEngineEventEntity::getProcessDefinitionId,
                        ProcessEngineEventEntity::getProcessInstanceId)
                .contains(
                        tuple("ActivityStartedEvent",
                              "2",
                              "3",
                              "4"),
                        tuple("ActivityCompletedEvent",
                              "11",
                              "23",
                              "42"));
    }

    private void waitForMessage() throws InterruptedException {
        //FIXME improve the waiting mechanism
        Thread.sleep(500);
    }

}
