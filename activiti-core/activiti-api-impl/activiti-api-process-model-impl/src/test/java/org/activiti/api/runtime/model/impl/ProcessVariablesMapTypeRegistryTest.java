/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

package org.activiti.api.runtime.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ProcessVariablesMapTypeRegistryTest {

    private static Stream<Arguments> typesToClass() {
        return Stream.of(
            Arguments.of("byte", Byte.class),
            Arguments.of("character", Character.class),
            Arguments.of("short", Short.class),
            Arguments.of("string", String.class),
            Arguments.of("long", Long.class),
            Arguments.of("integer", Integer.class),
            Arguments.of("boolean", Boolean.class),
            Arguments.of("double", Double.class),
            Arguments.of("float", Float.class),
            Arguments.of("date", Date.class),
            Arguments.of("localdate", LocalDate.class),
            Arguments.of("bigdecimal", BigDecimal.class),
            Arguments.of("json", JsonNode.class),
            Arguments.of("map", Map.class),
            Arguments.of("set", Set.class),
            Arguments.of("list", List.class)
        );
    }

    @ParameterizedTest
    @MethodSource("typesToClass")
    public void forType_shouldReturn_relatedClass(String type, Class<?> expectedClass) {
        //when
        Class<?> relatedClass = ProcessVariablesMapTypeRegistry.forType(type, String.class);

        //then
        assertThat(relatedClass).isEqualTo(expectedClass);
    }

    @Test
    public void forType_should_returnDefaultValue_whenTypeIsUnknown() {
        //given
        String type = "unknown";

        //when
        Class<?> relatedClass = ProcessVariablesMapTypeRegistry.forType(type, String.class);

        //then
        assertThat(relatedClass).isEqualTo(String.class);
    }
}
