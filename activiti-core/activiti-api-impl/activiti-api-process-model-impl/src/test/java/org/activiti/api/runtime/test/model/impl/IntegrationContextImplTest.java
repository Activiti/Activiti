/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.api.runtime.test.model.impl;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Currency;
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

import com.fasterxml.jackson.databind.DeserializationFeature;
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
                                             Arguments.of(String.valueOf("item1,item2"), String.valueOf("item1,item2")),
                                             Arguments.of(Boolean.valueOf(true), Boolean.valueOf(true)),
                                             Arguments.of('A', 'A'),
                                             Arguments.of(Character.valueOf('A'), Character.valueOf('A')),
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
                                             Arguments.of(Currency.getInstance("USD"), "USD"),
                                             Arguments.of(Date.from(instant), Date.from(instant)),
                                             Arguments.of(LocalDate.ofInstant(instant, ZoneOffset.UTC), LocalDate.ofInstant(instant, ZoneOffset.UTC)),
                                             Arguments.of(LocalDateTime.ofInstant(instant, ZoneOffset.UTC), LocalDateTime.ofInstant(instant, ZoneOffset.UTC)),
                                             Arguments.of(singletonList("item"), singletonList("item")),
                                             Arguments.of(singletonList(singletonMap("key", "value")), singletonList(singletonMap("key", "value"))),
                                             Arguments.of(singleton(singletonMap("key", "value")), singleton(singletonMap("key", "value"))),
                                             Arguments.of(singleton("item"), singleton("item")),
                                             Arguments.of(singletonMap("key", "value"), singletonMap("key", "value")),
                                             Arguments.of(JsonNodeFactory.instance.objectNode().set("key", TextNode.valueOf("value")),
                                                          JsonNodeFactory.instance.objectNode().set("key", TextNode.valueOf("value"))),
                                             Arguments.of(new CustomPojo("field1", "field2"),
                                                          new LinkedHashMap<String, String>() {{
                                                              put("field1", "field1");
                                                              put("field2", "field2");
                                                          }}),

                                             Arguments.of(new CustomPojoAnnotated("field1", "field2"),
                                                          new LinkedHashMap<String, String>() {{
                                                              put("@class", "org.activiti.api.runtime.test.model.impl.CustomPojoAnnotated");
                                                              put("field1", "field1");
                                                              put("field2", "field2");
                                                          }})
                                             };

    @SpringBootApplication
    static class Application {

        @Bean
        public ObjectMapper objectMapper(Module customizeProcessModelObjectMapper) {
            return new ObjectMapper().registerModule(customizeProcessModelObjectMapper)
                                     .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
    }

    private static Stream<Arguments> testIntegrationContextInBoundVariables() {
        return Stream.of(testValues);
    }

    @ParameterizedTest
    @MethodSource("testIntegrationContextInBoundVariables")
    public void testIntegrationContextInBoundVariables(Object input, Object output) throws IOException {
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
    @MethodSource("testIntegrationContextOutBoundVariables")
    public void testIntegrationContextOutBoundVariables(Object input, Object output) throws IOException {
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
