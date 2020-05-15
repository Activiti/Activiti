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
package org.activiti.api.runtime.model.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ProcessVariablesMapSerializer extends StdSerializer<ProcessVariablesMap<String, Object>> {

    private static final long serialVersionUID = 1L;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final ConversionService conversionService;
    private static List<Class<?>> scalarTypes = Arrays.asList(int.class,
                                                              byte.class,
                                                              short.class,
                                                              boolean.class,
                                                              long.class,
                                                              double.class,
                                                              float.class,
                                                              char.class,
                                                              Character.class,
                                                              Integer.class,
                                                              Byte.class,
                                                              Short.class,
                                                              Boolean.class,
                                                              Long.class,
                                                              Double.class,
                                                              Float.class,
                                                              BigDecimal.class,
                                                              Date.class,
                                                              String.class);

    private static Class<?>[] containerTypes = {Map.class,
                                                JsonNode.class,
                                                List.class,
                                                Set.class};

    public ProcessVariablesMapSerializer(ConversionService conversionService) {
        super(ProcessVariablesMap.class, true);

        this.conversionService = conversionService;
    }

    @Override
    public void serialize(ProcessVariablesMap<String, Object> processVariablesMap,
        JsonGenerator gen,
        SerializerProvider serializers) throws IOException {

        HashMap<String, ProcessVariableValue> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : processVariablesMap.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            map.put(name, buildProcessVariableValue(value));
        }

        gen.writeObject(map);
    }

    private ProcessVariableValue buildProcessVariableValue(Object value)
        throws JsonProcessingException {
        ProcessVariableValue variableValue = null;
        if (value != null) {
            Class<?> entryValueClass = value.getClass();

            boolean canConvert = conversionService.canConvert(entryValueClass, String.class);
            if (!canConvert) {
                value = objectMapper.writeValueAsString(value);
            }
            String entryType = resolveEntryType(entryValueClass, value, canConvert);
            String entryValue = conversionService.convert(value, String.class);

            variableValue = new ProcessVariableValue(entryType, entryValue);
        }
        return variableValue;
    }

    private String resolveEntryType(Class<?> clazz, Object value, boolean canConvert) {
        String entryType;

        if (scalarTypes.contains(clazz)) {
            entryType = clazz.getName();
        }
        else if (canConvert) {
            entryType = Stream.of(containerTypes)
                              .filter(type -> type.isInstance(value))
                              .findFirst()
                              .map(Class::getName)
                              .orElseGet(() -> clazz.getName());
        }
        else {
            entryType = Map.class.getName();
        }
        return entryType;
    }
}
