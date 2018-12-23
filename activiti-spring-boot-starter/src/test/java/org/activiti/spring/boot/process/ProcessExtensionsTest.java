package org.activiti.spring.boot.process;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.engine.ActivitiException;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.activiti.spring.process.variable.types.DateVariableType;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class ProcessExtensionsTest {

    private static final String INITIAL_VARS_PROCESS = "initialVarsProcess";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @After
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void processInstanceHasInitialVariables() {

        securityUtil.logInAs("salaboy");

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


        securityUtil.logInAs("salaboy");
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
                .withVariable("birth", new SimpleDateFormat(DateVariableType.defaultFormat).parse("2009-11-30"))
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
        securityUtil.logInAs("salaboy");
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

        securityUtil.logInAs("salaboy");
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        assertThatExceptionOfType(ActivitiException.class).isThrownBy(() -> {
            processRuntime.start(ProcessPayloadBuilder.start()
                    .withProcessDefinitionKey(INITIAL_VARS_PROCESS)
                    .withVariable("age", true)
                    .withVariable("name",7)
                    .withVariable("subscribe","ok")
                    .withVariable("birth","thisisnotadate")
                    .build());
        }).withMessage("Can't start process '" + INITIAL_VARS_PROCESS + "' as variables fail type validation - subscribe, name, birth, age");
    }
}
