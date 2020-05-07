package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.ActivitiException;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class ProcessExtensionsIT {

    private static final String INITIAL_VARS_PROCESS = "Process_initialVarsProcess";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @AfterEach
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @BeforeEach
    void setUp() {
        securityUtil.logInAs("user");
    }

    @Test
    public void processInstanceHasInitialVariables() {
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        // start a process with vars then check default and specified vars exist
        ProcessInstance initialVarsProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(INITIAL_VARS_PROCESS)
                .withVariable("extraVar",
                        true)
                .withVariable("age",
                        10)
                .withBusinessKey("my business key")
                .build());

        assertThat(initialVarsProcess).isNotNull();
        assertThat(initialVarsProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);

        List<VariableInstance> variableInstances = processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(initialVarsProcess).build());

        assertThat(variableInstances).isNotNull();
        assertThat(variableInstances).hasSize(4);

        assertThat(variableInstances).extracting("name")
                .contains("extraVar", "name", "age", "birth")
                .doesNotContain("subscribe");

        // cleanup
        processRuntime.delete(ProcessPayloadBuilder.delete(initialVarsProcess));
    }


    @Test
    public void processInstanceHasValidInitialVariables() throws ParseException {
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        // start a process with vars then check default and specified vars exist
        ProcessInstance initialVarsProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(INITIAL_VARS_PROCESS)
                .withVariable("extraVar",
                        true)
                .withVariable("age",
                        10)
                .withVariable("name",
                        "bob")
                .withVariable("subscribe",
                        true)
                .withVariable("birth", new SimpleDateFormat("yyyy-MM-dd").parse("2009-11-30"))
                .withBusinessKey("my business key")
                .build());

        assertThat(initialVarsProcess).isNotNull();
        assertThat(initialVarsProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);

        List<VariableInstance> variableInstances = processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(initialVarsProcess).build());

        assertThat(variableInstances).isNotNull();
        assertThat(variableInstances).hasSize(5);

        assertThat(variableInstances).extracting("name")
                .contains("extraVar", "name", "age", "birth","subscribe");

        // cleanup
        processRuntime.delete(ProcessPayloadBuilder.delete(initialVarsProcess));
    }


    @Test
    public void processInstanceFailsWithoutRequiredVariables() {
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        assertThatExceptionOfType(ActivitiException.class).isThrownBy(() -> {
            processRuntime.start(ProcessPayloadBuilder.start()
                    .withProcessDefinitionKey(INITIAL_VARS_PROCESS)
                    .withVariable("extraVar",
                            true)
                    .build());
        }).withMessage("Can't start process '" + INITIAL_VARS_PROCESS + "' without required variables - age");
    }

    @Test
    public void processInstanceFailsIfVariableTypeIncorrect() {
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
            processRuntime.start(ProcessPayloadBuilder.start()
                    .withProcessDefinitionKey(INITIAL_VARS_PROCESS)
                    .withVariable("age", true)
                    .withVariable("name",7)
                    .withVariable("subscribe","ok")
                    .withVariable("birth","2007-10-01")
                    .build());
        }).withMessage("Variables fail type validation: subscribe, name, age");
    }

    @Test
    public void should_mapProcessVariables_when_startEventMappingExists() {
        //given
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey("process-b42a166d-605b-4eec-8b96-82b1253666bf")
                .withVariable("Text0xfems",
                        "name_value")
                .withVariable("Text0rvs0o",
                        "email_value")
                .withBusinessKey("my business key")
                .build());

        //when
        List<VariableInstance> variableInstances = processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(process).build());

        //then
        assertThat(variableInstances)
            .isNotNull()
            .hasSize(2)
            .extracting(VariableInstance::getName,
                VariableInstance::getValue)
            .containsOnly(
                tuple("name", "name_value"),
                tuple("email", "email_value")
            );

        //when
        List<VariableInstance> taskVariables = retrieveVariablesForFirstTaskOfProcess(process);

        //then
        assertThat(taskVariables)
            .isNotNull()
            .hasSize(2)
            .extracting(VariableInstance::getName,
                VariableInstance::getValue)
            .containsOnly(
                tuple("nameInTask", "name_value"),
                tuple("emailInTask", "email_value")
            );

    }

    private List<VariableInstance> retrieveVariablesForFirstTaskOfProcess(
        ProcessInstance processInstance) {
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 1),
            TaskPayloadBuilder.tasksForProcess(processInstance).build());
        assertThat(tasks.getContent()).hasSize(1);
        return taskRuntime
            .variables(TaskPayloadBuilder.variables()
                .withTaskId(tasks.getContent().get(0).getId()).build());
    }

    @Test
    public void should_mapProcessVariables_when_createAndStartProcessAreCalled() {
        //given
        ProcessInstance createdProcess = processRuntime.create(ProcessPayloadBuilder.create()
            .withProcessDefinitionKey("process-b42a166d-605b-4eec-8b96-82b1253666bf")
            .withName("TEST")
            .build());

        ProcessInstance startedProcess = processRuntime.startCreatedProcess(createdProcess.getId(),
            ProcessPayloadBuilder.start()
            .withProcessDefinitionKey("process-b42a166d-605b-4eec-8b96-82b1253666bf")
            .withVariable("Text0xfems",
                "name_value")
            .withVariable("Text0rvs0o",
                "email_value")
            .withBusinessKey("my business key")
            .build() );

        //then
        List<VariableInstance> variableInstances = processRuntime.variables(ProcessPayloadBuilder.variables()
            .withProcessInstance(startedProcess).build());

        assertThat(variableInstances)
            .isNotNull()
            .hasSize(2)
            .extracting(VariableInstance::getName,
                VariableInstance::getValue)
            .containsOnly(
                tuple("name", "name_value"),
                tuple("email", "email_value")
            );

        //when
        List<VariableInstance> taskVariables = retrieveVariablesForFirstTaskOfProcess(
            createdProcess);


        //then
        assertThat(taskVariables)
            .isNotNull()
            .extracting(VariableInstance::getName,
                VariableInstance::getValue)
            .containsOnly(
                tuple("nameInTask", "name_value"),
                tuple("emailInTask", "email_value")
            );

    }
}
