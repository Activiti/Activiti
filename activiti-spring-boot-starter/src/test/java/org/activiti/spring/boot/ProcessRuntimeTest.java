package org.activiti.spring.boot;

import java.util.Arrays;

import org.activiti.runtime.api.ProcessRuntime;
import org.activiti.runtime.api.conf.ProcessRuntimeConfiguration;
import org.activiti.runtime.api.identity.ActivitiUser;
import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.model.ProcessDefinition;
import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.builders.ProcessPayloadBuilder;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.security.SecurityManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(ProcessRuntimeTestConfiguraiton.class)
public class ProcessRuntimeTest {

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private UserGroupManager userGroupManager;

    @Autowired
    private SecurityManager securityManager;

    private ActivitiUser salaboy;

    private ActivitiUser garth;

    private ActivitiUser admin;

    @Before
    public void init() {

        //Reset test variables

        ProcessRuntimeTestConfiguraiton.processImageConnectorExecuted = false;
        ProcessRuntimeTestConfiguraiton.tagImageConnectorExecuted = false;
        ProcessRuntimeTestConfiguraiton.discardImageConnectorExecuted = false;

        if (!userGroupManager.exists("admin")) {
            admin = userGroupManager.create("admin",
                    "password",
                    Arrays.asList("adminGroup"),
                    Arrays.asList("admin"));
        } else {
            admin = userGroupManager.loadUser("admin");
        }
        if (!userGroupManager.exists("salaboy")) {
            salaboy = userGroupManager.create("salaboy",
                    "password",
                    Arrays.asList("activitiTeam"),
                    Arrays.asList("user"));
        } else {
            salaboy = userGroupManager.loadUser("salaboy");
        }
        if (!userGroupManager.exists("garth")) {
            garth = userGroupManager.create("Garth",
                    "darkplace",
                    Arrays.asList("doctor"),
                    Arrays.asList("user"));
        } else {
            garth = userGroupManager.loadUser("garth");
        }


    }

    @After
    public void tearDown() {
        Page<ProcessInstance> processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances()
                        .build());
        for (ProcessInstance pi : processInstancePage.getContent()) {
            processRuntime.delete(ProcessPayloadBuilder.delete(pi));
        }


    }

    @Test
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
        assertThat(ProcessRuntimeTestConfiguraiton.processImageConnectorExecuted).isEqualTo(true);
        assertThat(ProcessRuntimeTestConfiguraiton.tagImageConnectorExecuted).isEqualTo(true);
        assertThat(ProcessRuntimeTestConfiguraiton.discardImageConnectorExecuted).isEqualTo(false);
    }

    @Test
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
        assertThat(ProcessRuntimeTestConfiguraiton.processImageConnectorExecuted).isEqualTo(true);
        assertThat(ProcessRuntimeTestConfiguraiton.tagImageConnectorExecuted).isEqualTo(false);
        assertThat(ProcessRuntimeTestConfiguraiton.discardImageConnectorExecuted).isEqualTo(true);
    }

    @Test
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


    }


    @Test
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


}
