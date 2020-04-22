/*
 * Copyright 2020 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.runtime.api.impl;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.process.model.payloads.VariableValue;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

public class VariableValuesPayloadConverter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final VariableValueConverter variableValueConverter;

    public VariableValuesPayloadConverter(VariableValueConverter variableValueConverter) {
        Assert.notNull(variableValueConverter, "VariableValueConverter must not be null");

        this.variableValueConverter = variableValueConverter;
    }

    public StartProcessPayload convert(StartProcessPayload from) {
        return ProcessPayloadBuilder.start(from)
                                    .withVariables(mapVariableValues(from.getVariables()))
                                    .build();

    }

    public Map<String, Object> mapVariableValues(Map<String, Object> input) {
        return input.entrySet()
                    .stream()
                    .map(this::parseValue)
                    .collect(LinkedHashMap::new, (m,v)->m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }

    private Map.Entry<String, Object> parseValue(Map.Entry<String, Object> entry) {
        Object value = entry.getValue();

        try {
            VariableValue variableValue = objectMapper.readValue(value.toString(),
                                                                 VariableValue.class);
            value = variableValueConverter.convert(variableValue);
        } catch (Exception ignored) { }

        return new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), value);
    }
}