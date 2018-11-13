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
import org.apache.commons.lang3.StringUtils;
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

        CustomType customType = new CustomType();
        customType.setCustomTypeField1("a");
        CustomType bigObject = new CustomType();
        bigObject.setCustomTypeField1(StringUtils.repeat("a", 4000));

        // start a process with vars then check default and specified vars exist
        ProcessInstance initialVarsProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(JSON_VARS_PROCESS)
                .withVariable("var2",new ObjectMapper().readValue("{ \"testvar2element\":\"testvar2element\"}", JsonNode.class))
                .withVariable("var4",customType)
                .withVariable("var5",new ObjectMapper().readValue("{ \"verylongjson\":\""+ StringUtils.repeat("a", 4000)+"\"}", JsonNode.class))
                .withVariable("var6",bigObject)
                .withBusinessKey("my business key")
                .build());


        assertThat(initialVarsProcess).isNotNull();
        assertThat(initialVarsProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);

        List<VariableInstance> variableInstances = processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(initialVarsProcess).build());

        assertThat(variableInstances).isNotNull();
        assertThat(variableInstances).hasSize(6);

        assertThat(variableInstances).extracting("name","type")
                .contains(tuple("var1","json"),
                        tuple("var2","json"),
                        tuple("var3","json"),
                        tuple("var4","json"),
                        tuple("var5","longJson"),
                        tuple("var6","longJson"));


        assertThat(variableInstances)
                .filteredOn("name","var2")
                .extracting("value")
                .hasSize(1)
                .extracting("class")
                .containsOnly(ObjectNode.class);

        assertThat(variableInstances)
                .filteredOn("name","var3")
                .extracting("value")
                .hasSize(1)
                .extracting("class")
                .containsOnly(ObjectNode.class);


        assertThat(variableInstances)
                .filteredOn("name","var4")
                .extracting("value")
                .hasSize(1)
                .extracting("class","customTypeField1")
                .containsOnly(tuple(CustomType.class,"a"));

        assertThat(variableInstances)
                .filteredOn("name","var6")
                .extracting("value")
                .hasSize(1)
                .extracting("class","customTypeField1")
                .containsOnly(tuple(CustomType.class,StringUtils.repeat("a", 4000)));

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
                    .withVariable("var5","this one is ok as doesn't have to be json")
                    .build());
        }).withMessage("Can't start process '" + JSON_VARS_PROCESS + "' as variables have unexpected types var2");
    }
}
