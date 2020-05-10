package org.activiti.api.runtime.test.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.runtime.model.impl.IntegrationContextImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class IntegrationContextImplTest {

    @Autowired
    private ObjectMapper objectMapper;

    private static Object[] testValues = {BigDecimal.valueOf(1000, 2),
                                          Long.valueOf(100000000000L),
                                          Integer.valueOf(123),
                                          String.valueOf("string"),
                                          Boolean.valueOf(true),
                                          Double.valueOf(123.123),
                                          Float.valueOf(123.123F),
                                          100000000000L,
                                          123,
                                          true,
                                          123.123,
                                          123.123f,
                                          null,
                                          Date.from(Instant.now()),
                                          Collections.singletonMap("key", "value")
                                          };
    @SpringBootApplication
    static class Application {

        @Bean
        public ObjectMapper objectMapper(Module customizeProcessModelObjectMapper) {
            return new ObjectMapper().registerModule(customizeProcessModelObjectMapper);
        }
    }

    private static Stream<Object> testIntegrationContextInBoundVariables() {
        return Stream.of(testValues);
    }

    @ParameterizedTest
    @MethodSource
    void testIntegrationContextInBoundVariables(Object value) throws JsonParseException, JsonMappingException, IOException {
        // given
        IntegrationContextImpl source = new IntegrationContextImpl();

        source.addInBoundVariable("variable",
                                  value);
        // when
        IntegrationContext target = exchangeIntegrationContext(source);

        // then
        assertThat(target.getInBoundVariables()).containsEntry("variable",
                                                               value);
    }

    private static Stream<Object> testIntegrationContextOutBoundVariables() {
        return Stream.of(testValues);
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegrationContextOutBoundVariables(Object value) throws JsonParseException, JsonMappingException, IOException {
        // given
        IntegrationContextImpl source = new IntegrationContextImpl();

        source.addOutBoundVariable("variable",
                                   value);

        // when
        IntegrationContext result = exchangeIntegrationContext(source);

        // then
        assertThat(result.getOutBoundVariables()).containsEntry("variable",
                                                                value);
    }

    private IntegrationContext exchangeIntegrationContext(IntegrationContext source) throws IOException {
        return objectMapper.readValue(objectMapper.writeValueAsString(source),
                                      IntegrationContext.class);
    }
}
