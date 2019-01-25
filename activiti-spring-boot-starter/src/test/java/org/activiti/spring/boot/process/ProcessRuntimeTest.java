package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.api.process.model.payloads.UpdateProcessPayload;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.runtime.api.impl.ProcessAdminRuntimeImpl;
import org.activiti.runtime.api.impl.ProcessRuntimeImpl;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeTest {

    private static final String CATEGORIZE_PROCESS = "categorizeProcess";
    private static final String CATEGORIZE_HUMAN_PROCESS = "categorizeHumanProcess";
    private static final String ONE_STEP_PROCESS = "OneStepProcess";
    
    private static final String SUB_PROCESS = "subProcess";
    private static final String SUPER_PROCESS = "superProcess";
    

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private APIProcessDefinitionConverter processDefinitionConverter;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ProcessSecurityPoliciesManager securityPoliciesManager;

    @Autowired
    private APIProcessInstanceConverter processInstanceConverter;

    @Autowired
    private APIVariableInstanceConverter variableInstanceConverter;

    @Autowired
    private ProcessRuntimeConfiguration configuration;
    
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private ApplicationEventPublisher eventPublisher;
    
    private ProcessRuntime processRuntimeMock;
    
    private ProcessAdminRuntime processAdminRuntimeMock;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @After
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Before
    public void init() {
        eventPublisher = spy(applicationEventPublisher);
        
        processRuntimeMock = spy(new ProcessRuntimeImpl(repositoryService,
                                                     processDefinitionConverter,
                                                     runtimeService,
                                                     securityPoliciesManager,
                                                     processInstanceConverter,
                                                     variableInstanceConverter,
                                                     configuration,
                                                     eventPublisher));

        processAdminRuntimeMock = spy(new ProcessAdminRuntimeImpl(repositoryService,
                                                              processDefinitionConverter,
                                                              runtimeService,
                                                              processInstanceConverter,
                                                              eventPublisher));

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
                .withVariable("name","garth")
                .withVariable("age",45)
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
                .withVariable("name","garth")
                .withVariable("age",45)
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

    @Test()
    public void adminFailTest() {
        securityUtil.logInAs("salaboy");
        //when
        Throwable throwable = catchThrowable(() -> processAdminRuntime.processInstance("fakeId"));
        //then
        assertThat(throwable)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test()
    public void userFailTest() {
        securityUtil.logInAs("admin");
        //when
        Throwable throwable = catchThrowable(() -> processRuntime.processDefinitions(Pageable.of(0,
                50)));
        //then
        assertThat(throwable)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void updateProcessInstance() {

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
                .withName("my process name")
                .build());

        assertThat(categorizeProcess).isNotNull();
        assertThat(categorizeProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);
        assertThat(categorizeProcess.getName()).isEqualTo("my process name");
        //assertThat(categorizeProcess.getDescription()).isNull();


        //
        //To do: currently Description is not possible to update
        //

       // update a process
        Page<ProcessInstance> processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50));

        ProcessInstance processInstance = processInstancePage.getContent().get(0);

        UpdateProcessPayload updateProcessPayload = ProcessPayloadBuilder.update()
                .withProcessInstanceId(processInstance.getId())
                .withBusinessKey(processInstance.getBusinessKey() + " UPDATED")
                .withName(processInstance.getName() + " UPDATED")
                .build();

        ProcessInstance updatedProcessInstance = processRuntime.update(updateProcessPayload);

        assertThat(updatedProcessInstance).isNotNull();

        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                                                                          50));

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);

        processInstance = processInstancePage.getContent().get(0);


        assertThat(processInstance.getName()).isEqualTo("my process name UPDATED");
        assertThat(processInstance.getBusinessKey()).isEqualTo("my business key UPDATED");


        // delete a process to avoid possible problems with other tests

        ProcessInstance deletedProcessInstance = processRuntime.delete(ProcessPayloadBuilder.delete(categorizeProcess));

        assertThat(deletedProcessInstance).isNotNull();
        assertThat(deletedProcessInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.DELETED);

    }

    @Test
    public void updateProcessInstanceAdmin() {

        securityUtil.logInAs("admin");

        Page<ProcessDefinition> processDefinitionPage = processAdminRuntime.processDefinitions(Pageable.of(0,
                50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent()).extracting((ProcessDefinition pd) -> pd.getKey())
                .contains(CATEGORIZE_HUMAN_PROCESS);


        // start a process with a business key to check filters
        ProcessInstance categorizeProcess = processAdminRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(CATEGORIZE_HUMAN_PROCESS)
                .withVariable("expectedKey",
                        true)
                .withBusinessKey("my business key")
                .withName("my process name")
                .build());

        assertThat(categorizeProcess).isNotNull();
        assertThat(categorizeProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);
        assertThat(categorizeProcess.getName()).isEqualTo("my process name");


        // update a process
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0,
                50));

        ProcessInstance processInstance = processInstancePage.getContent().get(0);

        UpdateProcessPayload updateProcessPayload = ProcessPayloadBuilder.update()
                .withProcessInstanceId(processInstance.getId())
                .withBusinessKey(processInstance.getBusinessKey() + " UPDATED")
                .withName(processInstance.getName() + " UPDATED")
                .build();

        ProcessInstance updatedProcessInstance = processAdminRuntime.update(updateProcessPayload);

        assertThat(updatedProcessInstance).isNotNull();

        processInstancePage = processAdminRuntime.processInstances(Pageable.of(0,
                                                                          50));

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);

        processInstance = processInstancePage.getContent().get(0);


        assertThat(processInstance.getName()).isEqualTo("my process name UPDATED");
        assertThat(processInstance.getBusinessKey()).isEqualTo("my business key UPDATED");


        // delete a process to avoid possible problems with other tests

        ProcessInstance deletedProcessInstance = processAdminRuntime.delete(ProcessPayloadBuilder.delete(categorizeProcess));

        assertThat(deletedProcessInstance).isNotNull();
        assertThat(deletedProcessInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.DELETED);

    }
    
    @Test
    public void getSubprocesses() {

        securityUtil.logInAs("salaboy");

        Page<ProcessInstance> processInstancePage;
        ProcessInstance parentProcess,subProcess;
      
        //given
        // start a process with a business key to check filters
        parentProcess=processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(SUPER_PROCESS)
                .withBusinessKey("my superprocess key")
                .build());

        //when
        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                50),
                ProcessPayloadBuilder
                        .processInstances()
                        .build());

        //Check that we have parent process and subprocess
        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(2);

        assertThat( processInstancePage.getContent().get(0).getProcessDefinitionKey()).isEqualTo(SUPER_PROCESS);
        assertThat( processInstancePage.getContent().get(1).getProcessDefinitionKey()).isEqualTo(SUB_PROCESS);
        
        
        //Check that parentProcess has 1 subprocess
        processInstancePage = processRuntime.processInstances(Pageable.of(0,
                                                                          50),
                                                                          ProcessPayloadBuilder
                                                                                  .subprocesses(parentProcess.getId()));
        
        
        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);
        
        subProcess=processInstancePage.getContent().get(0);
        
        assertThat(subProcess.getProcessDefinitionKey()).isEqualTo(SUB_PROCESS);
        assertThat(subProcess.getParentId()).isEqualTo(parentProcess.getId());
        assertThat(subProcess.getProcessDefinitionVersion()).isEqualTo(1);

        
        processRuntime.delete(ProcessPayloadBuilder.delete(subProcess));
        processRuntime.delete(ProcessPayloadBuilder.delete(parentProcess));
        
        

    }


    @Test
    public void signal() {
        securityUtil.logInAs("salaboy");

        // when
        SignalPayload signalPayload = new SignalPayload("The Signal", null);
        processRuntimeMock.signal(signalPayload);

        Page<ProcessInstance> processInstancePage = processRuntimeMock.processInstances(Pageable.of(0,
                50));

        // then
        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);
        assertThat(processInstancePage.getContent().get(0).getProcessDefinitionKey()).isEqualTo("processWithSignalStart1");
        
        verify(eventPublisher).publishEvent(signalPayload);
        
        processRuntimeMock.delete(ProcessPayloadBuilder.delete(processInstancePage.getContent().get(0).getId()));
    }

    @Test
    public void signalAdmin() {
        securityUtil.logInAs("admin");

        // when
        SignalPayload signalPayload = new SignalPayload("The Signal", null);
        processAdminRuntimeMock.signal(signalPayload);
        verify(eventPublisher).publishEvent(signalPayload);

        Page<ProcessInstance> processInstancePage = processAdminRuntimeMock.processInstances(Pageable.of(0,
                50));

        // then
        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).hasSize(1);
        assertThat(processInstancePage.getContent().get(0).getProcessDefinitionKey()).isEqualTo("processWithSignalStart1");

        processAdminRuntimeMock.delete(ProcessPayloadBuilder.delete(processInstancePage.getContent().get(0).getId()));
    }
}
