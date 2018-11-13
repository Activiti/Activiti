package org.activiti.spring.boot.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.engine.ActivitiException;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
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

    @Autowired
    private SecurityUtil securityUtil;


    @Test
    public void processInstanceHasValidInitialVariables() throws ParseException, IOException {

        securityUtil.logInAs("salaboy");

        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        CustomTypeAnnotated customTypeAnnotated = new CustomTypeAnnotated();
        CustomType customType = new CustomType();

        // start a process with vars then check default and specified vars exist
        ProcessInstance initialVarsProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(JSON_VARS_PROCESS)
                .withVariable("var2",new ObjectMapper().readValue("{ \"testvar2element\":\"testvar2element\"}", JsonNode.class))
                .withVariable("var3",customTypeAnnotated)
                .withVariable("var4",customType)
                .withBusinessKey("my business key")
                .build());

        //TODO: need to test with a long json var - >4000 chars

        //TODO: would like to be able to store without annotation
        //first try adding the custom type as an extra field in the json
        //then see if we can instead capture the type as the variable instance type
        //so any type other than the primitive types falls back on json
        //and support this both in the var instance and the modeler spec
        //put flag this so that old engine behaviour can be retained

        assertThat(initialVarsProcess).isNotNull();
        assertThat(initialVarsProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);

        List<VariableInstance> variableInstances = processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(initialVarsProcess).build());

        assertThat(variableInstances).isNotNull();
        assertThat(variableInstances).hasSize(5);

        assertThat(variableInstances).extracting("name","type")
                .contains(tuple("var1","json"),
                        tuple("var2","json"),
                        tuple("var3","json"),
                        tuple("var4","json"),
                        tuple("var5","json"));

        assertThat(variableInstances)
                .filteredOn("name","var3")
                .extracting("value")
                .hasSize(1)
                .extracting("class","customTypeField1")
                .containsOnly(tuple(CustomTypeAnnotated.class,null));

        assertThat(variableInstances)
                .filteredOn("name","var4")
                .extracting("value")
                .hasSize(1)
                .extracting("class","customTypeField1")
                .containsOnly(tuple(CustomType.class,null));

        assertThat(variableInstances)
                .filteredOn("name","var2")
                .extracting("value")
                .hasSize(1)
                .extracting("class")
                .containsOnly(ObjectNode.class);

        assertThat(variableInstances)
                .filteredOn("name","var5")
                .extracting("value")
                .hasSize(1)
                .extracting("class")
                .containsOnly(ObjectNode.class);

        // cleanup
        processRuntime.delete(ProcessPayloadBuilder.delete(initialVarsProcess));
    }


    @Test
    public void processInstanceFailsIfVariableTypeIncorrect() {
        securityUtil.logInAs("salaboy");
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
