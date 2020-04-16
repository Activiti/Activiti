package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.List;

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
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class ProcessExtensionsJsonVarsTest {

    private static final String JSON_VARS_PROCESS = "jsonVarsProcess";
    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @AfterEach
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void processInstanceHasValidInitialVariables() throws ParseException, IOException {

        securityUtil.logInAs("user");

        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        CustomType customType = new CustomType();
        customType.setCustomTypeField1("a");

        //because this annotated with type info it should automatically be deserialized to object
        CustomTypeAnnotated bigObject = new CustomTypeAnnotated();
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
                .hasOnlyElementsOfType(ObjectNode.class)
                .first()
                .toString()
                .equalsIgnoreCase("{ \"testvar2element\":\"testvar2element\"}");

        assertThat(variableInstances)
                .filteredOn("name","var3")
                .extracting("value")
                .hasSize(1)
                .hasOnlyElementsOfType(ObjectNode.class)
                .first()
                .toString()
                .contains("testvalueelement1");


        assertThat(variableInstances)
                .filteredOn("name","var4")
                .extracting("value")
                .hasSize(1)
                .hasOnlyElementsOfTypes(ObjectNode.class, CustomType.class)
                .toString()
                .contains(customType.getCustomTypeField1());

        assertThat(variableInstances)
                .filteredOn("name","var6")
                .extracting("value")
                .hasSize(1)
                .hasOnlyElementsOfType(CustomTypeAnnotated.class)
                .extracting("customTypeField1")
                .containsOnly(StringUtils.repeat("a", 4000));

        // cleanup
        processRuntime.delete(ProcessPayloadBuilder.delete(initialVarsProcess));
    }


    @Test
    public void processInstanceFailsIfVariableTypeIncorrect() {
        securityUtil.logInAs("user");
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        //by default jackson won't ser empty bean so it can't be handled as json
        assertThat(objectMapper.canSerialize(EmptyBean.class)).isFalse();

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
            processRuntime.start(ProcessPayloadBuilder.start()
                    .withProcessDefinitionKey(JSON_VARS_PROCESS)
                    .withVariable("var2", new EmptyBean())
                    .withVariable("var5","this one is ok as doesn't have to be json")
                    .build());
        }).withMessage("Variables fail type validation: var2");

        //we don't test for bad json in the extension file because the context doesn't load for that scenario as failure is during parsing
    }

    @Test
    public void processInstanceFailsIfVariableCannotBeSerializedAsJson(){

        securityUtil.logInAs("user");
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        //var5 doesn't ave any defined type as not in the file
        //but it still needs to be serlializable as json because serializePOJOsInVariablesToJson is true, meaning java ser is not available

        //by default jackson won't ser empty bean so it can't be handled as json
        assertThat(objectMapper.canSerialize(EmptyBean.class)).isFalse();

        assertThatExceptionOfType(ActivitiException.class).isThrownBy(() -> {
            processRuntime.start(ProcessPayloadBuilder.start()
                    .withProcessDefinitionKey(JSON_VARS_PROCESS)
                    .withVariable("var2",new ObjectMapper().readValue("{ \"testvar2element\":\"testvar2element\"}", JsonNode.class))
                    .withVariable("var5",new EmptyBean())
                    .build());
        }).withMessageStartingWith("couldn't find a variable type that is able to serialize");

    }

    //is serializable but not as json by default and java ser disabled at spring level by default
    static class EmptyBean implements Serializable { }


}
