package org.activiti.spring.boot.process;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeTest {

    private static final String CATEGORIZE_PROCESS = "categorizeProcess";
    private static final String CATEGORIZE_HUMAN_PROCESS = "categorizeHumanProcess";
    private static final String ONE_STEP_PROCESS = "OneStepProcess";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private SecurityUtil securityUtil;


    @Before
    public void init() {

        //Reset test variables
        RuntimeTestConfiguration.processImageConnectorExecuted = false;
        RuntimeTestConfiguration.tagImageConnectorExecuted = false;
        RuntimeTestConfiguration.discardImageConnectorExecuted = false;

    }

    @Test
    public void shouldGetConfiguration() {
        securityUtil.logInAs("salaboy");
        //when
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();

        //then
        assertThat(configuration).isNotNull();
    }

    @Test
    public void shouldGetAvailableProcessDefinitionForTheGivenUser() {

        securityUtil.logInAs("salaboy");

        //when
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
                                                                                                      50));
        //then
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent())
                .extracting(ProcessDefinition::getKey)
                .contains(CATEGORIZE_PROCESS,
                          CATEGORIZE_HUMAN_PROCESS,
                          ONE_STEP_PROCESS);
    }

    @Test
    public void createProcessInstanceAndValidateHappyPath() {

        securityUtil.logInAs("salaboy");

        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(CATEGORIZE_PROCESS)
                .withVariable("expectedKey",
                        true)
                .build());

        assertThat(RuntimeTestConfiguration.completedProcesses).contains(categorizeProcess.getId());
        //then
        assertThat(categorizeProcess).isNotNull();

        assertThat(categorizeProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.COMPLETED);
        assertThat(RuntimeTestConfiguration.processImageConnectorExecuted).isEqualTo(true);
        assertThat(RuntimeTestConfiguration.tagImageConnectorExecuted).isEqualTo(true);
        assertThat(RuntimeTestConfiguration.discardImageConnectorExecuted).isEqualTo(false);
    }

    @Test
    public void createProcessInstanceAndValidateDiscardPath() {

        securityUtil.logInAs("salaboy");

        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(CATEGORIZE_PROCESS)
                .withVariable("expectedKey",
                        false)
                .build());

        assertThat(RuntimeTestConfiguration.completedProcesses).contains(categorizeProcess.getId());

        //then
        assertThat(categorizeProcess).isNotNull();

        assertThat(categorizeProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.COMPLETED);
        assertThat(RuntimeTestConfiguration.processImageConnectorExecuted).isEqualTo(true);
        assertThat(RuntimeTestConfiguration.tagImageConnectorExecuted).isEqualTo(false);
        assertThat(RuntimeTestConfiguration.discardImageConnectorExecuted).isEqualTo(true);
    }

    @Test
    public void shouldGetProcessDefinitionFromDefinitionKey() {
        securityUtil.logInAs("salaboy");

        //when
        ProcessDefinition categorizeHumanProcess = processRuntime.processDefinition(CATEGORIZE_HUMAN_PROCESS);

        //then
        assertThat(categorizeHumanProcess).isNotNull();
        assertThat(categorizeHumanProcess.getName()).isEqualTo(CATEGORIZE_HUMAN_PROCESS);
        assertThat(categorizeHumanProcess.getId()).contains(CATEGORIZE_HUMAN_PROCESS);
    }

    @Test
    public void getProcessInstances() {

        securityUtil.logInAs("salaboy");

        //when
        Page<ProcessInstance> processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50));

        //then
        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(0);

        //given
        // start a process with a business key to check filters
        processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(CATEGORIZE_HUMAN_PROCESS)
                .withVariable("expectedKey",
                        true)
                .withBusinessKey("my business key")
                .build());

        //when
        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances()
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);

        // check for other key
        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances().withBusinessKey("other key")
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(0);

        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances().withBusinessKey("my business key")
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);

        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances()
                        .suspended()
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(0);

        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances()
                        .active()
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);

        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances()
                        .active()
                        .suspended()
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);

        ProcessInstance processInstance = processInstancePage.getContent().get(0);

        ProcessInstance suspendedProcessInstance = processRuntime.suspend(ProcessPayloadBuilder.suspend(processInstance));

        assertThat(suspendedProcessInstance).isNotNull();
        assertThat(suspendedProcessInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.SUSPENDED);

        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances()
                        .active()
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(0);

        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances()
                        .suspended()
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);

        processRuntime.resume(ProcessPayloadBuilder.resume(processInstance));

        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances()
                        .suspended()
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(0);

        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances()
                        .active()
                        .build());

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);

        ProcessInstance getSingleProcessInstance = processRuntime.processInstance(processInstance.getId());
        assertThat(getSingleProcessInstance).isNotNull();
        assertThat(getSingleProcessInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);

        // I need to clean up the Process Instances that I started because @WithMockUser cannot be used in @Before method
        ProcessInstance deletedProcessInstance = processRuntime.delete(ProcessPayloadBuilder.delete(getSingleProcessInstance));
        assertThat(deletedProcessInstance).isNotNull();

        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50));

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(0);

    }


    @Test
    public void deleteProcessInstance() {

        securityUtil.logInAs("salaboy");

        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
                50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent()).extracting((ProcessDefinition pd) -> pd.getKey())
                .contains(CATEGORIZE_HUMAN_PROCESS);


        // start a process with a business key to check filters
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(CATEGORIZE_HUMAN_PROCESS)
                .withVariable("expectedKey",
                        true)
                .withBusinessKey("my business key")
                .build());

        assertThat(categorizeProcess).isNotNull();
        assertThat(categorizeProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);

        Page<ProcessInstance> processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50));

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);

        ProcessInstance deletedProcessInstance = processRuntime.delete(ProcessPayloadBuilder.delete(categorizeProcess));

        assertThat(deletedProcessInstance).isNotNull();
        assertThat(deletedProcessInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.DELETED);


        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50));

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(0);

    }

    @Test(expected = AccessDeniedException.class)
    public void adminFailTest() {
        securityUtil.logInAs("salaboy");
        ProcessInstance fakeId = processAdminRuntime.processInstance("fakeId");
    }

    @Test(expected = AccessDeniedException.class)
    public void userFailTest() {
        securityUtil.logInAs("admin");
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
                50));
    }


}
