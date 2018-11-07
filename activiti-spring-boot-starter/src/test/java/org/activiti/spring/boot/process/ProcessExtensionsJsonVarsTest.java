package org.activiti.spring.boot.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.engine.ActivitiException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class ProcessExtensionsJsonVarsTest {

    private static final String JSON_VARS_PROCESS = "jsonVarsProcess";

    @Autowired
    private ProcessRuntime processRuntime;

    @Test
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void processInstanceHasValidInitialVariables() throws ParseException, IOException {

        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        // start a process with vars then check default and specified vars exist
        ProcessInstance initialVarsProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(JSON_VARS_PROCESS)
                .withVariable("var2",new ObjectMapper().readValue("{ \"testvar2element\":\"testvar2element\"}", HashMap.class))
                .withBusinessKey("my business key")
                .build());

        assertThat(initialVarsProcess).isNotNull();
        assertThat(initialVarsProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);

        List<VariableInstance> variableInstances = processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(initialVarsProcess).build());

        assertThat(variableInstances).isNotNull();
        assertThat(variableInstances).hasSize(2);

        //TODO: want the type to be json but currently is serializable
        //see classes under org.activiti.engine.impl.variable
        assertThat(variableInstances).extracting("name","type")
                .contains(tuple("var1","json"),
                        tuple("var2","json"));


        // cleanup
        processRuntime.delete(ProcessPayloadBuilder.delete(initialVarsProcess));
    }


    @Test
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void processInstanceFailsIfVariableTypeIncorrect() {
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        assertThatExceptionOfType(ActivitiException.class).isThrownBy(() -> {
            processRuntime.start(ProcessPayloadBuilder.start()
                    .withProcessDefinitionKey(JSON_VARS_PROCESS)
                    .withVariable("var2", "thisisn'tjson")
                    .withVariable("var3","this one is ok as doesn't have to be json")
                    .build());
        }).withMessage("Can't start process '" + JSON_VARS_PROCESS + "' as variables have unexpected types var2");
    }
}
