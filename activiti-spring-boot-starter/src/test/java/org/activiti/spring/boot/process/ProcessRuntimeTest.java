package org.activiti.spring.boot.process;

import org.activiti.runtime.api.ProcessAdminRuntime;
import org.activiti.runtime.api.ProcessRuntime;
import org.activiti.runtime.api.conf.ProcessRuntimeConfiguration;
import org.activiti.runtime.api.model.ProcessDefinition;
import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.builders.ProcessPayloadBuilder;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.spring.boot.RuntimeTestConfiguration;
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
@ContextConfiguration
public class ProcessRuntimeTest {

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private UserDetailsService userDetailsService;

    @Before
    public void init() {

        //Reset test variables
        RuntimeTestConfiguration.processImageConnectorExecuted = false;
        RuntimeTestConfiguration.tagImageConnectorExecuted = false;
        RuntimeTestConfiguration.discardImageConnectorExecuted = false;

    }


    @Test
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void createProcessInstanceAndValidateHappyPath() {

        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
                50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent())
                .extracting((ProcessDefinition pd) -> pd.getKey())
                .contains("categorizeProcess");

        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey("categorizeProcess")
                .withVariable("expectedKey",
                        true)
                .build());

        assertThat(categorizeProcess).isNotNull();

        assertThat(categorizeProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.COMPLETED);
        assertThat(RuntimeTestConfiguration.processImageConnectorExecuted).isEqualTo(true);
        assertThat(RuntimeTestConfiguration.tagImageConnectorExecuted).isEqualTo(true);
        assertThat(RuntimeTestConfiguration.discardImageConnectorExecuted).isEqualTo(false);
    }

    @Test
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void createProcessInstanceAndValidateDiscardPath() {

        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
                50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent())
                .extracting((ProcessDefinition pd) -> pd.getKey())
                .contains("categorizeProcess");

        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey("categorizeProcess")
                .withVariable("expectedKey",
                        false)
                .build());

        assertThat(categorizeProcess).isNotNull();

        assertThat(categorizeProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.COMPLETED);
        assertThat(RuntimeTestConfiguration.processImageConnectorExecuted).isEqualTo(true);
        assertThat(RuntimeTestConfiguration.tagImageConnectorExecuted).isEqualTo(false);
        assertThat(RuntimeTestConfiguration.discardImageConnectorExecuted).isEqualTo(true);
    }

    @Test
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void getProcessInstances() {

        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
                50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent()).extracting((ProcessDefinition pd) -> pd.getKey())
                .contains("categorizeHumanProcess");

        ProcessDefinition categorizeHumanProcess = processRuntime.processDefinition("categorizeHumanProcess");
        assertThat(categorizeHumanProcess).isNotNull();
        assertThat(categorizeHumanProcess.getName()).isEqualTo("categorizeHumanProcess");
        assertThat(categorizeHumanProcess.getId()).contains("categorizeHumanProcess");

        Page<ProcessInstance> processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50));

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(0);

        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances()
                        .build());


        // start a process with a business key to check filters
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey("categorizeHumanProcess")
                .withVariable("expectedKey",
                        true)
                .withBusinessKey("my business key")
                .build());

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

        ProcessInstance resumedProcessInstance = processRuntime.resume(ProcessPayloadBuilder.resume(processInstance));

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
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void deleteProcessInstance() {

        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
                50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent()).extracting((ProcessDefinition pd) -> pd.getKey())
                .contains("categorizeHumanProcess");


        // start a process with a business key to check filters
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey("categorizeHumanProcess")
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
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void adminFailTest() {
        ProcessInstance fakeId = processAdminRuntime.processInstance("fakeId");
    }

    @Test(expected = AccessDeniedException.class)
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "myUserDetailsService")
    public void userFailTest() {
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
                50));
    }


}
