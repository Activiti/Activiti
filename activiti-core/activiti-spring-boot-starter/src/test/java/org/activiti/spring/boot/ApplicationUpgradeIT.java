package org.activiti.spring.boot;

import java.util.ArrayList;
import java.util.List;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.core.common.project.model.ProjectManifest;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ApplicationUpgradeIT {

    private static final String PROCESS_DEFINITION_KEY = "SingleTaskProcess";
    private static final String PROCESS_NAME = "single-task";
    private static final String SINGLE_TASK_PROCESS_DEFINITION_PATH = "processes/SingleTaskProcess.bpmn20.xml";
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
        deploySingleTaskProcess(projectManifest);

        projectManifest.setVersion("34");
        deploySingleTaskProcess(projectManifest);

        securityUtil.logInAs("user");

        ProcessDefinition result = processRuntime.processDefinition(PROCESS_DEFINITION_KEY);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(PROCESS_NAME);
        assertThat(result.getId()).contains(PROCESS_DEFINITION_KEY);
        assertThat(result.getAppVersion()).isEqualTo("2");

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

    private void deploySingleTaskProcess(ProjectManifest projectManifest) {
        Deployment deployment = repositoryService.createDeployment()
            .setProjectManifest(projectManifest)
            .enableDuplicateFiltering()
            .tenantId("tenantId")
            .name(DEPLOYMENT_TYPE_NAME)
            .addClasspathResource(SINGLE_TASK_PROCESS_DEFINITION_PATH)
            .deploy();
        deploymentIds.add(deployment.getId());
    }

}
