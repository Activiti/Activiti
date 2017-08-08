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

package org.activiti.services.audit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.services.audit.events.ActivityStartedEventEntity;
import org.activiti.services.audit.events.ActivityStartedEventEntityAssert;
import org.activiti.services.audit.events.ProcessEngineEventEntity;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class AuditServiceIT {

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
        List<ProcessEngineEvent> coveredEvents = getCoveredEvents();
        for (ProcessEngineEvent event : coveredEvents) {
            producer.send(event);
        }
        waitForMessage();

        //when
        ResponseEntity<PagedResources<ProcessEngineEventEntity>> eventsPagedResources = eventsRestTemplate.executeFindAll();

        //then
        Collection<ProcessEngineEventEntity> retrievedEvents = eventsPagedResources.getBody().getContent();
        assertThat(retrievedEvents).hasSameSizeAs(coveredEvents);
        for (ProcessEngineEvent coveredEvent : coveredEvents) {
            assertThat(retrievedEvents)
                    .extracting(
                            ProcessEngineEventEntity::getEventType,
                            ProcessEngineEventEntity::getExecutionId,
                            ProcessEngineEventEntity::getProcessDefinitionId,
                            ProcessEngineEventEntity::getProcessInstanceId)
                    .contains(tuple(coveredEvent.getEventType(),
                                    coveredEvent.getExecutionId(),
                                    coveredEvent.getProcessDefinitionId(),
                                    coveredEvent.getProcessInstanceId()));
        }
    }

    private List<ProcessEngineEvent> getCoveredEvents() {
        List<ProcessEngineEvent> coveredEvents = new ArrayList<>();
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ActivityCancelledEvent",
                                                     "100",
                                                     "103",
                                                     "104"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ActivityStartedEvent",
                                                     "2",
                                                     "3",
                                                     "4"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ActivityCompletedEvent",
                                                     "11",
                                                     "23",
                                                     "42"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ProcessCompletedEvent",
                                                     "12",
                                                     "24",
                                                     "43"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ProcessCancelledEvent",
                                                     "112",
                                                     "124",
                                                     "143"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "ProcessStartedEvent",
                                                     "13",
                                                     "25",
                                                     "44"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "SequenceFlowTakenEvent",
                                                     "14",
                                                     "26",
                                                     "45"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "TaskAssignedEvent",
                                                     "15",
                                                     "27",
                                                     "46"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "TaskCompletedEvent",
                                                     "16",
                                                     "28",
                                                     "47"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "TaskCreatedEvent",
                                                     "17",
                                                     "29",
                                                     "48"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "VariableCreatedEvent",
                                                     "18",
                                                     "30",
                                                     "49"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "VariableDeletedEvent",
                                                     "19",
                                                     "31",
                                                     "50"));
        coveredEvents.add(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                     "VariableUpdatedEvent",
                                                     "20",
                                                     "32",
                                                     "51"));
        return coveredEvents;
    }

    private void waitForMessage() throws InterruptedException {
        //FIXME improve the waiting mechanism
        Thread.sleep(500);
    }

    @Test
    public void findByIdShouldReturnTheEventIdentifiedByTheGivenId() throws Exception {
        //given
        producer.send(new MockProcessEngineEvent(System.currentTimeMillis(),
                                                 "ActivityStartedEvent",
                                                 "2",
                                                 "3",
                                                 "4"));

        waitForMessage();

        ResponseEntity<PagedResources<ProcessEngineEventEntity>> eventsPagedResources = eventsRestTemplate.executeFindAll();
        assertThat(eventsPagedResources.getBody().getContent()).isNotEmpty();
        ProcessEngineEventEntity event = eventsPagedResources.getBody().getContent().iterator().next();

        //when
        ResponseEntity<ProcessEngineEventEntity> responseEntity = eventsRestTemplate.executeFindById(event.getId());

        //then
        assertThat(responseEntity.getBody()).isInstanceOf(ActivityStartedEventEntity.class);
        ActivityStartedEventEntityAssert.assertThat((ActivityStartedEventEntity) responseEntity.getBody())
                .hasId(event.getId())
                .hasEventType("ActivityStartedEvent")
                .hasExecutionId("2")
                .hasProcessDefinitionId("3")
                .hasProcessInstanceId("4");
    }

}
