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
import java.util.List;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.core.common.project.model.ProjectManifest;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
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

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    private List<String> deploymentIds;

    @BeforeEach
    public void setUp() {
        deploymentIds = new ArrayList<>();
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

        securityUtil.logInAs("user");

        ProcessDefinition result = processRuntime.processDefinition(
            SINGLE_TASK_PROCESS_DEFINITION_KEY);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(PROCESS_NAME);
        assertThat(result.getId()).contains(SINGLE_TASK_PROCESS_DEFINITION_KEY);
        assertThat(result.getAppVersion()).isEqualTo(String.valueOf(latestDeployment.getVersion()));

    }

    @Test
    public void processDefinitions_should_returnOnlyTheProcessesInTheLatest_when_multipleVersions() {
        ProjectManifest projectManifest = new ProjectManifest();
        projectManifest.setVersion("12");
        deployProcesses(projectManifest, SINGLE_TASK_PROCESS_DEFINITION_PATH,
            MULTI_INSTANCE_PROCESS_DEFINITION_PATH);

        projectManifest.setVersion("34");
        Deployment latestDeployment = deployProcesses(projectManifest,
            MULTI_INSTANCE_PROCESS_DEFINITION_PATH);

        securityUtil.logInAs("user");

        Page<ProcessDefinition> result = processRuntime.processDefinitions(Pageable.of(0, 100));
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

    private Deployment deployProcesses(ProjectManifest projectManifest, String ... processPaths) {
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
            .setProjectManifest(projectManifest)
            .enableDuplicateFiltering()
            .name(DEPLOYMENT_TYPE_NAME);
        for (String processPath : processPaths) {
            deploymentBuilder.addClasspathResource(processPath);
        }
        Deployment deployment = deploymentBuilder
            .deploy();
        deploymentIds.add(deployment.getId());
        return deployment;
    }

}
