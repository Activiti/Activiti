package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.math.BigDecimal;
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
    public void processInstanceHasValidInitialVariables() {
        securityUtil.logInAs("user");
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        assertThat(configuration).isNotNull();

        VariableValue jsonNodeValue = new VariableValue("JsonNode", "{}");
        Map<String, String> stringValue = new VariableValue("string", "name").toMap();
        Map<String, String> intValue = new VariableValue("integer", "10").toMap();
        Map<String, String> booleanValue = new VariableValue("boolean", "true").toMap();
        Map<String, String> doubleValue = new VariableValue("double", "10.00").toMap();
        Map<String, String> dateValue = new VariableValue("date", DATE_1970_01_01T01_01_01_001Z).toMap();
        Map<String, String> bigDecimalValue = new VariableValue("BigDecimal", "10.00").toMap();

        // start a process with vars then check default and specified vars exist
        ProcessInstance initialVarsProcess = processRuntime.start(ProcessPayloadBuilder.start()
                                                                                       .withProcessDefinitionKey("SingleTaskProcess")
                                                                                       .withVariable("jsonNodeValue", jsonNodeValue)
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
                                     .hasSize(12)
                                     .extracting("name","value")
                                     .contains(tuple("jsonNodeValue",JsonNodeFactory.instance.objectNode()),
                                               tuple("stringValue","name"),
                                               tuple("intValue", 10),
                                               tuple("doubleValue", 10.00),
                                               tuple("booleanValue", true),
                                               tuple("dateValue", dateFormatterProvider.parse(DATE_1970_01_01T01_01_01_001Z)),
                                               tuple("bigDecimalValue", BigDecimal.valueOf(1000, 2)),
                                               tuple("int", 10),
                                               tuple("double", 10.00),
                                               tuple("boolean", true),
                                               tuple("string", "name"),
                                               tuple("date", dateFormatterProvider.parse(DATE_1970_01_01T01_01_01_001Z)));

        // cleanup
        processRuntime.delete(ProcessPayloadBuilder.delete(initialVarsProcess));
    }


    @Test
    public void testVariableValuesMapper() {
        // given
        VariableValuesPayloadConverter subject = new VariableValuesPayloadConverter(variableValueConverter);

        Map<String, Object> input = new HashMap<>();

        input.put("int", 123);
        input.put("string", "123");
        input.put("bool", true);
        input.put("nullValue", new VariableValue("String", null).toMap());
        input.put("stringValue", new VariableValue("string", "name").toMap());
        input.put("quoteValue", new VariableValue("string", "\"").toMap());
        input.put("intValue", new VariableValue("int", "10").toMap());
        input.put("longValue", new VariableValue("long", "10").toMap());
        input.put("booleanValue", new VariableValue("boolean", "true").toMap());
        input.put("doubleValue", new VariableValue("double", "10.00").toMap());
        input.put("localDateValue", new VariableValue("LocalDate", "2020-04-20").toMap());
        input.put("dateValue", new VariableValue("Date", DATE_1970_01_01T01_01_01_001Z).toMap());
        input.put("bigDecimalValue", new VariableValue("BigDecimal", "10.00").toMap());
        input.put("jsonNodeValue", new VariableValue("JsonNode", "{}").toMap());
        input.put("jsonNodeValue2", new VariableValue("JsonNode", "{}"));
        input.put("mapValue", new VariableValue("Map", "{}").toMap());

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
                          .containsEntry("bigDecimalValue", BigDecimal.valueOf(1000, 2))
                          .containsEntry("jsonNodeValue", JsonNodeFactory.instance.objectNode())
                          .containsEntry("jsonNodeValue2", JsonNodeFactory.instance.objectNode())
                          .containsEntry("mapValue", Collections.emptyMap());
    }

    @Test
    public void testVariableValueConverter() {
        // when
        String nullValue = variableValueConverter.convert(new VariableValue("String", null));
        String stringValue = variableValueConverter.convert(new VariableValue("string", "name"));
        Integer intValue = variableValueConverter.convert(new VariableValue("int", "10"));
        Long longValue = variableValueConverter.convert(new VariableValue("long", "10"));
        Boolean booleanValue = variableValueConverter.convert(new VariableValue("boolean", "true"));
        Double doubleValue = variableValueConverter.convert(new VariableValue("double", "10.00"));
        LocalDate localDateValue = variableValueConverter.convert(new VariableValue("LocalDate", "2020-04-20"));
        Date dateValue = variableValueConverter.convert(new VariableValue("Date", DATE_1970_01_01T01_01_01_001Z));
        BigDecimal bigDecimalValue = variableValueConverter.convert(new VariableValue("BigDecimal", "10.00"));
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
        assertThat(bigDecimalValue).isEqualTo(BigDecimal.valueOf(1000, 2));
        assertThat(jsonNodeValue).isEqualTo(JsonNodeFactory.instance.objectNode());
        assertThat(mapValue).isEqualTo(Collections.emptyMap());
    }




}
