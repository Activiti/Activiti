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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.core.common.project.model.ProjectManifest;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.Job;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ApplicationUpgradeIT {

    private static final String SINGLE_TASK_PROCESS_DEFINITION_KEY = "SingleTaskProcess";
    private static final String PROCESS_NAME = "single-task";
    private static final String SINGLE_TASK_PROCESS_DEFINITION_PATH = "processes/SingleTaskProcess.bpmn20.xml";
    private static final String MULTI_INSTANCE_PROCESS_DEFINITION_PATH = "processes/multi-instance-parallel-all-output-data-ref.bpmn20.xml";
    private static final String MULTI_INSTANCE_PROCESS_DEFINITION_KEY = "miParallelUserTasksAllOutputCollection";
    private static final String DEPLOYMENT_TYPE_NAME = "SpringAutoDeployment";
    private static final String PROCESS_FROM_CUSTOM_DEPLOYMENT_KEY = "ProcessFromCustomDeployment";
    private static final String ANOTHER_PROCESS_FROM_CUSTOM_DEPLOYMENT_KEY = "AnotherProcessFromCustomDeployment";

    @Autowired
    private ActivitiProperties activitiProperties;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfigurationImpl;

    @Autowired
    private SecurityUtil securityUtil;

    private EventSubscriptionQueryImpl eventSubscriptionQuery;

    private List<String> deploymentIds;

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
    public void should_updateDeploymentVersion_when_manifestIsPresent() {
        ProjectManifest projectManifest = new ProjectManifest();
        projectManifest.setVersion("7");

        Deployment deployment1 = repositoryService.createDeployment()
                .setProjectManifest(projectManifest)
                .enableDuplicateFiltering()
                .name("deploymentName")
                .deploy();
        deploymentIds.add(deployment1.getId());

        assertThat(deployment1.getVersion()).isEqualTo(1);
        assertThat(deployment1.getProjectReleaseVersion()).isEqualTo("7");

        projectManifest.setVersion("17");

        Deployment deployment2 = repositoryService.createDeployment()
                .setProjectManifest(projectManifest)
                .enableDuplicateFiltering()
                .name("deploymentName")
                .deploy();
        deploymentIds.add(deployment2.getId());

        assertThat(deployment2.getProjectReleaseVersion()).isEqualTo("17");
        assertThat(deployment2.getVersion()).isEqualTo(2);
    }

    @Test
    public void should_getLatestProcessDefinitionByKey_when_multipleVersions() {
        ProjectManifest projectManifest = new ProjectManifest();
        projectManifest.setVersion("12");
        deployProcesses(projectManifest, SINGLE_TASK_PROCESS_DEFINITION_PATH);

        projectManifest.setVersion("34");
        Deployment latestDeployment = deployProcesses(projectManifest, SINGLE_TASK_PROCESS_DEFINITION_PATH);

        ProcessDefinition result = processRuntime.processDefinition(
            SINGLE_TASK_PROCESS_DEFINITION_KEY);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(PROCESS_NAME);
        assertThat(result.getId()).contains(SINGLE_TASK_PROCESS_DEFINITION_KEY);
        assertThat(result.getAppVersion()).isEqualTo(String.valueOf(latestDeployment.getVersion()));

    }

    @Test
    public void should_adminApiGetLatestProcessDefinitionByKey_when_multipleVersions() {
        ProjectManifest projectManifest = new ProjectManifest();
        projectManifest.setVersion("12");
        deployProcesses(projectManifest, SINGLE_TASK_PROCESS_DEFINITION_PATH);

        projectManifest.setVersion("34");
        Deployment latestDeployment = deployProcesses(projectManifest, SINGLE_TASK_PROCESS_DEFINITION_PATH);

        securityUtil.logInAs("admin");

        ProcessDefinition result = processAdminRuntime.processDefinition(
            SINGLE_TASK_PROCESS_DEFINITION_KEY);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(PROCESS_NAME);
        assertThat(result.getId()).contains(SINGLE_TASK_PROCESS_DEFINITION_KEY);
        assertThat(result.getAppVersion()).isEqualTo(String.valueOf(latestDeployment.getVersion()));
    }

    @Test
    public void processDefinitions_should_returnOnlyTheLatestVersion_when_multipleVersions() {
        //given
        ProjectManifest projectManifest = new ProjectManifest();
        projectManifest.setVersion("12");
        deployProcesses(projectManifest, SINGLE_TASK_PROCESS_DEFINITION_PATH,
            MULTI_INSTANCE_PROCESS_DEFINITION_PATH);

        projectManifest.setVersion("34");
        Deployment latestDeployment = deployProcesses(projectManifest,
            MULTI_INSTANCE_PROCESS_DEFINITION_PATH);

        //when
        Page<ProcessDefinition> result = processRuntime.processDefinitions(Pageable.of(0, 100));

        //then
        assertThat(result.getContent())
            .filteredOn(processDefinition -> processDefinition.getKey().equals(
                SINGLE_TASK_PROCESS_DEFINITION_KEY) || processDefinition.getKey()
                .equals(MULTI_INSTANCE_PROCESS_DEFINITION_KEY))
            .extracting(ProcessDefinition::getKey, ProcessDefinition::getVersion,
                ProcessDefinition::getAppVersion)
            .containsExactly(
                tuple(MULTI_INSTANCE_PROCESS_DEFINITION_KEY, latestDeployment.getVersion(),
                    String.valueOf(latestDeployment.getVersion())));

    }

    @Test
    public void processDefinitions_should_returnProcesses_when_deploymentIsCreatedWithoutProjectManifest() {
        //given
        deployProcessesWithoutProjectManifest("customDeployment",
            "custom-deployment/ProcessFromCustomDeployment.bpmn20.xml");

        //when
        Page<ProcessDefinition> result = processRuntime.processDefinitions(Pageable.of(0, 100));

        //then
        assertThat(result.getContent())
            .filteredOn(processDefinition ->
                PROCESS_FROM_CUSTOM_DEPLOYMENT_KEY.equals(processDefinition.getKey()))
            .extracting(ProcessDefinition::getKey, ProcessDefinition::getVersion,
                ProcessDefinition::getAppVersion)
            .containsExactly(
                tuple(PROCESS_FROM_CUSTOM_DEPLOYMENT_KEY, 1, null));

    }

    @Test
    public void processDefinitions_should_returnOnlyTheLatestVersion_when_deploymentIsCreatedWithoutManifestAndIsUpdatedWithManifest() {
        //given
        ProjectManifest projectManifest = new ProjectManifest();
        projectManifest.setVersion("12");

        String deploymentName = "customDeployment";
        deployProcessesWithoutProjectManifest(deploymentName,
            "custom-deployment/ProcessFromCustomDeployment.bpmn20.xml");
        Deployment latestCustomDeployment = deployProcesses(deploymentName, projectManifest,
            "custom-deployment/AnotherProcessFromCustomDeployment.bpmn20.xml");

        //when
        Page<ProcessDefinition> result = processRuntime.processDefinitions(Pageable.of(0, 100));

        //then
        assertThat(result.getContent())
            .filteredOn(processDefinition -> Arrays.asList(
                PROCESS_FROM_CUSTOM_DEPLOYMENT_KEY,
                ANOTHER_PROCESS_FROM_CUSTOM_DEPLOYMENT_KEY)
                .contains(processDefinition.getKey()))
            .extracting(ProcessDefinition::getKey, ProcessDefinition::getVersion,
                ProcessDefinition::getAppVersion)
            .containsExactly(
                tuple(ANOTHER_PROCESS_FROM_CUSTOM_DEPLOYMENT_KEY, latestCustomDeployment.getVersion(),
                    String.valueOf(latestCustomDeployment.getVersion())));

    }

    @Test
    public void should_updateDeploymentVersion_when_onlyEnforcedAppVersionIsSet(){

        Deployment deployment1 = repositoryService.createDeployment()
            .setEnforcedAppVersion(1)
            .enableDuplicateFiltering()
            .name("deploymentName")
            .deploy();
        deploymentIds.add(deployment1.getId());
        assertThat(deployment1.getVersion()).isEqualTo(1);

        Deployment deployment2 = repositoryService.createDeployment()
            .setEnforcedAppVersion(2)
            .enableDuplicateFiltering()
            .name("deploymentName")
            .deploy();
        deploymentIds.add(deployment2.getId());
        assertThat(deployment2.getVersion()).isEqualTo(2);
    }

    @Test
    public void should_updateDeploymentVersion_when_onlyProjectManifestVersionIsSet(){
        ProjectManifest projectManifest = new ProjectManifest();
        projectManifest.setVersion("2");

        Deployment deployment1 = repositoryService.createDeployment()
            .setProjectManifest(projectManifest)
            .enableDuplicateFiltering()
            .name("deploymentName")
            .deploy();
        deploymentIds.add(deployment1.getId());

        assertThat(deployment1.getVersion()).isEqualTo(1);
        assertThat(deployment1.getProjectReleaseVersion()).isEqualTo("2");

        projectManifest.setVersion("17");

        Deployment deployment2 = repositoryService.createDeployment()
            .setProjectManifest(projectManifest)
            .setEnforcedAppVersion(2)
            .enableDuplicateFiltering()
            .name("deploymentName")
            .deploy();
        deploymentIds.add(deployment2.getId());
        assertThat(deployment2.getVersion()).isEqualTo(2);
        assertThat(deployment2.getProjectReleaseVersion()).isEqualTo("17");
    }

    @Test
    public void should_enforcedAppVersionTakePriorityOverProjectManifestVersion() {
        ProjectManifest projectManifest = new ProjectManifest();
        projectManifest.setVersion("2");

        Deployment deployment1 = repositoryService.createDeployment()
            .setEnforcedAppVersion(1)
            .setProjectManifest(projectManifest)
            .enableDuplicateFiltering()
            .name("deploymentName")
            .deploy();
        deploymentIds.add(deployment1.getId());

        assertThat(deployment1.getVersion()).isEqualTo(1);
        assertThat(deployment1.getProjectReleaseVersion()).isEqualTo("2");

        projectManifest.setVersion("17");

        Deployment deployment2 = repositoryService.createDeployment()
            .setEnforcedAppVersion(5)
            .setProjectManifest(projectManifest)
            .enableDuplicateFiltering()
            .name("deploymentName")
            .deploy();
        deploymentIds.add(deployment2.getId());
        assertThat(deployment2.getVersion()).isEqualTo(5);
        assertThat(deployment2.getProjectReleaseVersion()).isEqualTo("17");
    }

    @Test
    public void should_noUpgradeTakePlace_when_enforcedAppVersionAndProjectManifestVersionAreNotSet() {

        Deployment deployment1 = repositoryService.createDeployment()
            .enableDuplicateFiltering()
            .name("deploymentName")
            .deploy();
        deploymentIds.add(deployment1.getId());

        assertThat(deployment1.getVersion()).isEqualTo(1);

        Deployment deployment2 = repositoryService.createDeployment()
            .enableDuplicateFiltering()
            .name("deploymentName")
            .deploy();
        deploymentIds.add(deployment2.getId());

        assertThat(deployment2.getVersion()).isEqualTo(1);
    }

    private Deployment deployProcesses(String deploymentName, ProjectManifest projectManifest,
        String... processPaths) {
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
            .setProjectManifest(projectManifest)
            .enableDuplicateFiltering()
            .name(deploymentName);
        for (String processPath : processPaths) {
            deploymentBuilder.addClasspathResource(processPath);
        }
        Deployment deployment = deploymentBuilder
            .deploy();
        deploymentIds.add(deployment.getId());
        return deployment;
    }

    private Deployment deployProcesses(ProjectManifest projectManifest, String ... processPaths) {
        return deployProcesses(DEPLOYMENT_TYPE_NAME, projectManifest, processPaths);
    }

    private Deployment deployProcessesWithoutProjectManifest(String deploymentName, String ... processPaths) {
        return deployProcesses(deploymentName, null, processPaths);
    }

    @Test
    public void disableAllPreviousStartEvents_shouldBeFalse_when_notSet() {
        assertThat(activitiProperties.shouldDisableExistingStartEventSubscriptions()).isFalse();
    }

    @Test
    public void should_notDeletePreviousTimerStartEvents_when_projectIsUpgraded_and_disableStartEventsIsFalse() {
        String deploymentName = "startEventDeployment";

        String deploymentId = deployProcess(deploymentName, "processes/ProcessWithTimerStartEvent.bpmn20.xml");

        org.activiti.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).list().getFirst();
        List<Job> jobs = managementService.createTimerJobQuery().processDefinitionId(processDefinition.getId()).list();

        assertThat(jobs)
            .extracting(Job::getProcessDefinitionId)
            .containsExactly(processDefinition.getId());

        deployProcess(deploymentName, "processes/ProcessWithoutTimerStartEvent.bpmn20.xml");

        assertThat(jobs)
            .extracting(Job::getProcessDefinitionId)
            .containsExactly(processDefinition.getId());
    }

    @Test
    public void should_notDeletePreviousMessageStartEvents_when_projectIsUpgraded_and_disableStartEventsIsFalse() {
        String deploymentName = "testDeployment";
        deployProcess(deploymentName, "processes/ProcessWithMessageStartEvent.bpmn20.xml");

        List<EventSubscriptionEntity> messageSubscriptions = eventSubscriptionQuery.eventType("message").activityId("MessageStartEvent").list();
        assertThat(messageSubscriptions).hasSize(1);

        deployProcess(deploymentName, "processes/ProcessWithoutMessageStartEvent.bpmn20.xml");

        messageSubscriptions = eventSubscriptionQuery.eventType("message").activityId("MessageStartEvent").list();
        assertThat(messageSubscriptions).hasSize(1);
    }

    @Test
    public void should_notDeletePreviousSignalStartEvents_when_projectIsUpgraded_and_disableStartEventsIsFalse() {
        String deploymentName = "signalDeployment";

        deployProcess(deploymentName, "processes/ProcessWithSignalStartEvent.bpmn20.xml");

        List<EventSubscriptionEntity> signalSubscriptions = eventSubscriptionQuery.eventType("signal").activityId("SignalStartEvent").list();
        assertThat(signalSubscriptions).hasSize(1);

        deployProcess(deploymentName, "processes/ProcessWithoutSignalStartEvent.bpmn20.xml");

        signalSubscriptions = eventSubscriptionQuery.eventType("signal").activityId("SignalStartEvent").list();
        assertThat(signalSubscriptions).hasSize(1);
    }

    private String deployProcess(String deploymentName, String processPath) {
        Deployment deployment = repositoryService.createDeployment()
            .addClasspathResource(processPath).
            name(deploymentName).deploy();
        deploymentIds.add(deployment.getId());
        return deployment.getId();
    }
}
