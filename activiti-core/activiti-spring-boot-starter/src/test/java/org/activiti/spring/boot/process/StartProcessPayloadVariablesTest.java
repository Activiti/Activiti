package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.VariableValue;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.runtime.api.impl.VariableValueConverter;
import org.activiti.runtime.api.impl.VariableValuesPayloadConverter;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class StartProcessPayloadVariablesTest {

    private static final String DATE_1970_01_01T01_01_01_001Z = "1970-01-01T01:01:01.001Z";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private VariableValueConverter variableValueConverter;

    @Autowired
    private DateFormatterProvider dateFormatterProvider;

    @AfterEach
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void processInstanceHasValidInitialVariables() throws ParseException, JsonProcessingException {
        securityUtil.logInAs("user");
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        String stringValue = new VariableValue("String", "name").toString();
        String intValue = new VariableValue("Integer", "10").toString();
        String booleanValue = new VariableValue("Boolean", "true").toString();
        String doubleValue = new VariableValue("Double", "10.00").toString();
        String dateValue = new VariableValue("Date", DATE_1970_01_01T01_01_01_001Z).toString();
        String bigDecimalValue = new VariableValue("BigDecimal", "10.00").toString();

        // start a process with vars then check default and specified vars exist
        ProcessInstance initialVarsProcess = processRuntime.start(ProcessPayloadBuilder.start()
                                                                                       .withProcessDefinitionKey("SingleTaskProcess")
                                                                                       .withVariable("stringValue", stringValue)
                                                                                       .withVariable("intValue", intValue)
                                                                                       .withVariable("doubleValue", doubleValue)
                                                                                       .withVariable("booleanValue", booleanValue)
                                                                                       .withVariable("dateValue", dateValue)
                                                                                       .withVariable("bigDecimalValue", bigDecimalValue)
                                                                                       .withVariable("int", 10)
                                                                                       .withVariable("boolean", true)
                                                                                       .withVariable("double", 10.00)
                                                                                       .withVariable("string", "name")
                                                                                       .withVariable("date", dateFormatterProvider.parse(DATE_1970_01_01T01_01_01_001Z))
                                                                                       .build());
        assertThat(initialVarsProcess).isNotNull();
        assertThat(initialVarsProcess.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);

        List<VariableInstance> variableInstances = processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(initialVarsProcess).build());

        assertThat(variableInstances).isNotNull()
                                     .hasSize(11)
                                     .extracting("name","value")
                                     .contains(tuple("stringValue","name"),
                                               tuple("intValue", 10),
                                               tuple("doubleValue", 10.00),
                                               tuple("booleanValue", true),
                                               tuple("dateValue", dateFormatterProvider.parse(DATE_1970_01_01T01_01_01_001Z)),
                                               tuple("bigDecimalValue", BigDecimal.valueOf(10.00)),
                                               tuple("int", 10),
                                               tuple("double", 10.00),
                                               tuple("boolean", true),
                                               tuple("string", "name"),
                                               tuple("date", dateFormatterProvider.parse(DATE_1970_01_01T01_01_01_001Z)));

        // cleanup
        processRuntime.delete(ProcessPayloadBuilder.delete(initialVarsProcess));
    }


    @Test
    public void testVariableValuesMapper() throws ParseException, JsonProcessingException {
        // given
        VariableValuesPayloadConverter subject = new VariableValuesPayloadConverter(variableValueConverter);

        Map<String, Object> input = new HashMap<>();

        input.put("int", 123);
        input.put("string", "123");
        input.put("bool", true);
        input.put("nullValue", new VariableValue("String", null).toString());
        input.put("stringValue", new VariableValue("string", "name").toString());
        input.put("quoteValue", new VariableValue("string", "\"").toString());
        input.put("intValue", new VariableValue("int", "10").toString());
        input.put("longValue", new VariableValue("long", "10").toString());
        input.put("booleanValue", new VariableValue("boolean", "true").toString());
        input.put("doubleValue", new VariableValue("double", "10.00").toString());
        input.put("localDateValue", new VariableValue("LocalDate", "2020-04-20").toString());
        input.put("dateValue", new VariableValue("Date", DATE_1970_01_01T01_01_01_001Z).toString());
        input.put("bigDecimalValue", new VariableValue("BigDecimal", "10.01").toString());
        input.put("jsonNodeValue", new VariableValue("JsonNode", "{}").toString());
        input.put("mapValue", new VariableValue("Map", "{}").toString());

        // when
        Map<String, Object> result = subject.mapVariableValues(input);

        // then
        assertThat(result).containsEntry("int", 123)
                          .containsEntry("string", "123")
                          .containsEntry("bool", true)
                          .containsEntry("nullValue", null)
                          .containsEntry("stringValue", "name")
                          .containsEntry("quoteValue", "\"")
                          .containsEntry("intValue", 10)
                          .containsEntry("longValue", 10L)
                          .containsEntry("booleanValue", true)
                          .containsEntry("doubleValue", 10.00)
                          .containsEntry("localDateValue", LocalDate.of(2020, 4, 20))
                          .containsEntry("dateValue", dateFormatterProvider.parse(DATE_1970_01_01T01_01_01_001Z))
                          .containsEntry("bigDecimalValue", BigDecimal.valueOf(10.01))
                          .containsEntry("jsonNodeValue", JsonNodeFactory.instance.objectNode())
                          .containsEntry("mapValue", Collections.emptyMap());
    }

    @Test
    public void testVariableValueConverter() throws ParseException, JsonProcessingException {
        // when
        String nullValue = variableValueConverter.convert(new VariableValue("String", null));
        String stringValue = variableValueConverter.convert(new VariableValue("string", "name"));
        Integer intValue = variableValueConverter.convert(new VariableValue("int", "10"));
        Long longValue = variableValueConverter.convert(new VariableValue("long", "10"));
        Boolean booleanValue = variableValueConverter.convert(new VariableValue("boolean", "true"));
        Double doubleValue = variableValueConverter.convert(new VariableValue("double", "10.00"));
        LocalDate localDateValue = variableValueConverter.convert(new VariableValue("LocalDate", "2020-04-20"));
        Date dateValue = variableValueConverter.convert(new VariableValue("Date", DATE_1970_01_01T01_01_01_001Z));
        BigDecimal bigDecimalValue = variableValueConverter.convert(new VariableValue("BigDecimal", "10.01"));
        JsonNode jsonNodeValue = variableValueConverter.convert(new VariableValue("JsonNode", "{}"));
        Map<String, Object> mapValue = variableValueConverter.convert(new VariableValue("Map", "{}"));

        // then
        assertThat(nullValue).isNull();
        assertThat(stringValue).isEqualTo("name");
        assertThat(intValue).isEqualTo(10);
        assertThat(longValue).isEqualTo(10L);
        assertThat(booleanValue).isEqualTo(true);
        assertThat(doubleValue).isEqualTo(10.00);
        assertThat(localDateValue).isEqualTo(LocalDate.of(2020, 4, 20));
        assertThat(dateValue).isEqualTo(dateFormatterProvider.parse(DATE_1970_01_01T01_01_01_001Z));
        assertThat(bigDecimalValue).isEqualTo(BigDecimal.valueOf(10.01));
        assertThat(jsonNodeValue).isEqualTo(JsonNodeFactory.instance.objectNode());
        assertThat(mapValue).isEqualTo(Collections.emptyMap());
    }




}
