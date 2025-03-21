/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.spring.boot;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.Job;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {"spring.activiti.disable-existing-start-event-subscriptions=true"})
public class ApplicationUpgradeWithStartEventsIT {

    @Autowired
    private ActivitiProperties activitiProperties;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfigurationImpl;


    private List<String> deploymentIds;

    private EventSubscriptionQueryImpl eventSubscriptionQuery;

    @BeforeEach
    public void setUp() {
        deploymentIds = new ArrayList<>();
        securityUtil.logInAs("user");
        eventSubscriptionQuery = new EventSubscriptionQueryImpl(processEngineConfigurationImpl.getCommandExecutor());
    }

    @AfterEach
    public void tearDown() {
        deploymentIds.forEach(deploymentId -> repositoryService.deleteDeployment(deploymentId, true));
    }

    @Test
    public void testDisableAllPreviousStartEvents() {
        assertThat(activitiProperties.shouldDisableExistingStartEventSubscriptions()).isTrue();
    }

    @Test
    public void should_deletePreviousTimerStartEvents_when_projectIsUpgraded() {
        String deploymentName = "testDeployment";
        deployProcess(deploymentName, "processes/ProcessWithTimerStartEvent.bpmn20.xml");

        List<Job> list = managementService.createTimerJobQuery().list();
        assertThat(list).hasSize(1);

        deployProcess(deploymentName, "processes/ProcessWithoutTimerStartEvent.bpmn20.xml");

        list = managementService.createTimerJobQuery().list();
        assertThat(list).hasSize(0);
    }

    @Test
    public void should_deletePreviousMessageStartEvents_when_projectIsUpgraded() {
        String deploymentName = "testDeployment";

        deployProcess(deploymentName, "processes/ProcessWithMessageStartEvent.bpmn20.xml");

        List<EventSubscriptionEntity> messageSubscriptions = eventSubscriptionQuery.eventType("message").activityId("MessageStartEvent").list();
        assertThat(messageSubscriptions).hasSize(1);

        deployProcess(deploymentName, "processes/ProcessWithoutMessageStartEvent.bpmn20.xml");

        messageSubscriptions = eventSubscriptionQuery.eventType("message").activityId("MessageStartEvent").list();
        assertThat(messageSubscriptions).hasSize(0);
    }

    @Test
    public void should_deletePreviousSignalStartEvents_when_projectIsUpgraded() {
        String deploymentName = "signalDeployment";

        deployProcess(deploymentName, "processes/ProcessWithSignalStartEvent.bpmn20.xml");

        List<EventSubscriptionEntity> signalSubscriptions = eventSubscriptionQuery.eventType("signal").activityId("SignalStartEvent").list();
        assertThat(signalSubscriptions).hasSize(1);

        deployProcess(deploymentName, "processes/ProcessWithoutSignalStartEvent.bpmn20.xml");

        signalSubscriptions = eventSubscriptionQuery.eventType("signal").activityId("SignalStartEvent").list();
        assertThat(signalSubscriptions).hasSize(0);
    }

    private String deployProcess(String deploymentName, String processPath) {
        Deployment deployment = repositoryService.createDeployment()
            .addClasspathResource(processPath).
            name(deploymentName).deploy();
        deploymentIds.add(deployment.getId());
        return deployment.getId();
    }
}
