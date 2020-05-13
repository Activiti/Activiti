package org.activiti.api.runtime.test.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.stream.Stream;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.runtime.model.impl.IntegrationContextImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class IntegrationContextImplTest {

    @Autowired
    private ObjectMapper objectMapper;

    private static Instant instant = Instant.now();

    private static Arguments[] testValues = {Arguments.of(BigDecimal.valueOf(1000, 2), BigDecimal.valueOf(1000, 2)),
                                             Arguments.of(Long.valueOf(100000000000L), Long.valueOf(100000000000L)),
                                             Arguments.of(Integer.valueOf(123), Integer.valueOf(123)),
                                             Arguments.of(String.valueOf("string"), String.valueOf("string")),
                                             Arguments.of(Boolean.valueOf(true), Boolean.valueOf(true)),
                                             Arguments.of(Double.valueOf(123.123), Double.valueOf(123.123)),
                                             Arguments.of(Float.valueOf(123.123F), Float.valueOf(123.123F)),
                                             Arguments.of(Byte.valueOf("1"), Byte.valueOf("1")),
                                             Arguments.of(Short.valueOf("1"), Short.valueOf("1")),
                                             Arguments.of(Float.valueOf(123.123F), Float.valueOf(123.123F)),
                                             Arguments.of(100000000000L, 100000000000L),
                                             Arguments.of(123, 123),
                                             Arguments.of(true, true),
                                             Arguments.of(123.123, 123.123),
                                             Arguments.of(123.123f, 123.123f),
                                             Arguments.of(null, null),
                                             Arguments.of(Date.from(instant), Date.from(instant)),
                                             Arguments.of(Collections.singletonMap("key", "value"),
                                                          Collections.singletonMap("key", "value")),
                                             Arguments.of(JsonNodeFactory.instance.objectNode().set("key", TextNode.valueOf("value")),
                                                          JsonNodeFactory.instance.objectNode().set("key", TextNode.valueOf("value"))),
                                             Arguments.of(new CustomPojo("field1", "field2"),
                                                          new LinkedHashMap<>() {{
                                                              put("field1", "field1");
                                                              put("field2", "field2");
                                                          }}),

                                             Arguments.of(new CustomPojoAnnotated("field1", "field2"),
                                                          new LinkedHashMap<>() {{
                                                              put("@class", "org.activiti.api.runtime.test.model.impl.CustomPojoAnnotated");
                                                              put("field1", "field1");
                                                              put("field2", "field2");
                                                          }})
                                             };

    @SpringBootApplication
    static class Application {

        @Bean
        public ObjectMapper objectMapper(Module customizeProcessModelObjectMapper) {
            return new ObjectMapper().registerModule(customizeProcessModelObjectMapper);
        }
    }

    private static Stream<Arguments> testIntegrationContextInBoundVariables() {
        return Stream.of(testValues);
    }

    @ParameterizedTest
    @MethodSource
    void testIntegrationContextInBoundVariables(Object input,
                                                Object output) throws JsonParseException,
                                                                      JsonMappingException,
                                                                      IOException {
        // given
        IntegrationContextImpl source = new IntegrationContextImpl();

        source.addInBoundVariable("variable",
                                  input);
        // when
        IntegrationContext target = exchangeIntegrationContext(source);

        // then
        assertThat(target.getInBoundVariables()).containsEntry("variable",
                                                               output);
    }

    private static Stream<Arguments> testIntegrationContextOutBoundVariables() {
        return Stream.of(testValues);
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegrationContextOutBoundVariables(Object input,
                                                        Object output) throws JsonParseException,
                                                                              JsonMappingException,
                                                                              IOException {
        // given
        IntegrationContextImpl source = new IntegrationContextImpl();

        source.addOutBoundVariable("variable",
                                   input);

        // when
        IntegrationContext target = exchangeIntegrationContext(source);

        // then
        assertThat(target.getOutBoundVariables()).containsEntry("variable",
                                                                output);
    }

    private IntegrationContext exchangeIntegrationContext(IntegrationContext source) throws IOException {
        return objectMapper.readValue(objectMapper.writeValueAsString(source),
                                      IntegrationContext.class);
    }
}
