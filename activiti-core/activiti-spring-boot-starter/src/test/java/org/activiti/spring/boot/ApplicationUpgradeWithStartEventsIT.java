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
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,properties = {"spring.activiti.disable-all-previous-start-events=true"})
public class ApplicationUpgradeWithStartEventsIT {

    @Autowired
    private ActivitiProperties activitiProperties;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    ManagementService managementService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;


    private List<String> deploymentIds;

    @BeforeEach
    public void setUp() {
        deploymentIds = new ArrayList<>();
        securityUtil.logInAs("user");
    }

    @AfterEach
    public void tearDown() {
        deploymentIds.forEach(deploymentId -> repositoryService.deleteDeployment(deploymentId, true));
    }

    @Test
    public void testDisableAllPreviousStartEvents() {
        assert activitiProperties.isDisableAllPreviousStartEvents();
    }

    @Test
    public void should_deletePreviousTimerStartEvents_when_projectIsUpgraded() {
        String deploymentName = "testDeployment";
        Deployment oldDeployment = repositoryService.createDeployment()
            .addClasspathResource("processes/ProcessWithTimerStartEvent.bpmn20.xml").
            name(deploymentName).deploy();
        deploymentIds.add(oldDeployment.getId());
        List<Job> list = managementService.createTimerJobQuery().list();
        assertThat(list).hasSize(1);
        Deployment newDeployment = repositoryService.createDeployment()
            .addClasspathResource("processes/ProcessWithoutTimerStartEvent.bpmn20.xml").
            name(deploymentName).deploy();
        deploymentIds.add(newDeployment.getId());
        list = managementService.createTimerJobQuery().list();
        assertThat(list).hasSize(0);
    }

    /*@Test
    public void should_deletePreviousMessageStartEvents_when_projectIsUpgraded() {
        String deploymentName = "testDeployment";
        Deployment oldDeployment = repositoryService.createDeployment()
            .addClasspathResource("processes/ProcessWithMessageStartEvent.bpmn20.xml").
            name(deploymentName).deploy();
        deploymentIds.add(oldDeployment.getId());
        eventSubscriptionEntityManager.findMessageStartEventSubscriptions();
        Deployment newDeployment = repositoryService.createDeployment()
            .addClasspathResource("processes/ProcessWithoutMessageStartEvent.bpmn20.xml").
            name(deploymentName).deploy();
        deploymentIds.add(newDeployment.getId());
        System.out.println(MessageTestConfiguration.messageSubscriptionCancelledEvents.get(0).getProcessDefinitionId());
        assertThat(MessageTestConfiguration.messageSubscriptionCancelledEvents).hasSize(1);
    }*/

        /*@Test
        public void should_deletePreviousSignalStartEvents_when_projectIsUpgraded() {
            String deploymentName = "testDeployment";
            repositoryService.createDeployment()
                .addClasspathResource("processes/ProcessWithSignalStartEvent.bpmn20.xml").
                name(deploymentName).deploy();
           var listOf =runtimeService.getProcessInstanceEvents("ProcessWithSignalStartEvent");
           System.out.println(listOf.size());
           var list = managementService.createJobQuery().list();
            assertThat(list).hasSize(1);
            repositoryService.createDeployment()
                .addClasspathResource("processes/ProcessWithoutSignalStartEvent.bpmn20.xml").
                name(deploymentName).deploy();
            assertThat(list).hasSize(0);
        }*/

}
