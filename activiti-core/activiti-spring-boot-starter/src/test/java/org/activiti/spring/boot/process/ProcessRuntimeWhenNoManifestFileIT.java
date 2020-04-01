package org.activiti.spring.boot.process;

import static org.activiti.api.process.model.ProcessInstance.ProcessInstanceStatus.COMPLETED;
import static org.activiti.api.process.model.ProcessInstance.ProcessInstanceStatus.RUNNING;
import static org.activiti.spring.boot.RuntimeTestConfiguration.completedProcesses;
import static org.activiti.spring.boot.RuntimeTestConfiguration.discardImageConnectorExecuted;
import static org.activiti.spring.boot.RuntimeTestConfiguration.processImageConnectorExecuted;
import static org.activiti.spring.boot.RuntimeTestConfiguration.tagImageConnectorExecuted;
import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.process.model.Deployment;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.application.conf.ApplicationProcessAutoConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = "project.manifest.file.path=null")
@EnableAutoConfiguration(exclude = ApplicationProcessAutoConfiguration.class)
public class ProcessRuntimeWhenNoManifestFileIT {

    private static final String CATEGORIZE_PROCESS = "categorizeProcess";
    private static final String CATEGORIZE_HUMAN_PROCESS = "categorizeHumanProcess";
    private static final String ONE_STEP_PROCESS = "OneStepProcess";

    private static final String SUPER_PROCESS = "superProcess";
    public static final String TAG_OR_DISCARD_IMAGE_CONNECTOR_KEY = "expectedKey";
    private static final Pageable DEFAULT_PAGEABLE = Pageable.of(0, 50);

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @After
    public void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Before
    public void init() {
        //Reset test variables
        processImageConnectorExecuted = false;
        tagImageConnectorExecuted = false;
        discardImageConnectorExecuted = false;
    }

    @Test
    public void should_getConfiguration() {
        securityUtil.logInAs("user");

        ProcessRuntimeConfiguration configuration = processRuntime.configuration();

        assertThat(configuration).isNotNull();
    }

    @Test
    public void should_getAvailableProcessDefinitionsForTheGivenUser() {
        securityUtil.logInAs("user");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(DEFAULT_PAGEABLE);

        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent())
            .extracting(ProcessDefinition::getKey)
            .contains(CATEGORIZE_PROCESS, CATEGORIZE_HUMAN_PROCESS)
            .doesNotContain(ONE_STEP_PROCESS);
    }

    @Test
    public void should_createProcessInstanceWithoutCompletingIt_when_createIsCalled() {
        securityUtil.logInAs("user");

        ProcessInstance categorizeProcess = processRuntime.create(ProcessPayloadBuilder.start()
            .withProcessDefinitionKey(CATEGORIZE_PROCESS)
            .build());

        assertThat(categorizeProcess).isNotNull();
        assertThat(completedProcesses).doesNotContain(categorizeProcess.getId());
        assertThat(categorizeProcess.getStatus()).isEqualTo(RUNNING);
        assertThat(processImageConnectorExecuted).isEqualTo(false);
    }

    @Test
    public void should_createProcessInstanceAndCompleteHappyPath_when_startIsCalled() {
        securityUtil.logInAs("user");

        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
            .withProcessDefinitionKey(CATEGORIZE_PROCESS)
            .withVariable(TAG_OR_DISCARD_IMAGE_CONNECTOR_KEY, true)
            .build());

        assertThat(categorizeProcess).isNotNull();
        assertThat(completedProcesses).contains(categorizeProcess.getId());
        assertThat(categorizeProcess.getStatus()).isEqualTo(COMPLETED);
        assertThat(processImageConnectorExecuted).isEqualTo(true);
        assertThat(tagImageConnectorExecuted).isEqualTo(true);
        assertThat(discardImageConnectorExecuted).isEqualTo(false);
    }

    @Test
    public void should_createProcessInstanceAndCompleteDiscardPath() {
        securityUtil.logInAs("user");

        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
            .withProcessDefinitionKey(CATEGORIZE_PROCESS)
            .withVariable(TAG_OR_DISCARD_IMAGE_CONNECTOR_KEY, false)
            .build());

        assertThat(categorizeProcess).isNotNull();
        assertThat(completedProcesses).contains(categorizeProcess.getId());
        assertThat(categorizeProcess.getStatus()).isEqualTo(COMPLETED);
        assertThat(processImageConnectorExecuted).isEqualTo(true);
        assertThat(tagImageConnectorExecuted).isEqualTo(false);
        assertThat(discardImageConnectorExecuted).isEqualTo(true);
    }

    @Test
    public void should_getProcessDefinition_fromDefinitionKey() {
        securityUtil.logInAs("user");

        //when
        ProcessDefinition categorizeHumanProcess = processRuntime.processDefinition(CATEGORIZE_HUMAN_PROCESS);

        //then
        assertThat(categorizeHumanProcess).isNotNull();
        assertThat(categorizeHumanProcess.getId()).contains(CATEGORIZE_HUMAN_PROCESS);
        assertThat(categorizeHumanProcess.getName()).isEqualTo(CATEGORIZE_HUMAN_PROCESS);
    }

    @Test
    public void should_latestDeploymentHaveProjectReleaseVersionAsNull_when_noManifestFile() {
        securityUtil.logInAs("user");

        Deployment deployment = processRuntime.selectLatestDeployment();

        assertThat(deployment.getVersion()).isEqualTo(1);
        assertThat(deployment.getProjectReleaseVersion()).isNull();
        assertThat(deployment.getName()).isEqualTo("SpringAutoDeployment");
    }

    @Test
    public void should_processDefinitionsHaveAppVersionAsNull_when_noManifestFile() {
        securityUtil.logInAs("user");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(DEFAULT_PAGEABLE);

        assertThat(processDefinitionPage.getContent())
            .isNotEmpty()
            .extracting(ProcessDefinition::getAppVersion)
            .containsOnlyNulls();
    }

    @Test
    public void should_processInstanceHaveAppVersionAsNull_when_noManifestFile() {
        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
            .withProcessDefinitionKey(SUPER_PROCESS)
            .build());

        assertThat(processInstance.getAppVersion()).isNull();
    }

}
