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

public class VariableValuesPayloadConverter {

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
        Object entryValue = entry.getValue();

        try {
            if(Map.class.isInstance(entryValue)) {
                Map<String, String> valuesMap = Map.class.cast(entryValue);

                if(valuesMap.containsKey("type") && valuesMap.containsKey("value")) {
                    String type = (String) valuesMap.get("type");
                    String value = (String) valuesMap.get("value");

                    entryValue = variableValueConverter.convert(new VariableValue(type, value));
                }

            } else if (VariableValue.class.isInstance(entryValue)) {
                entryValue = variableValueConverter.convert(VariableValue.class.cast(entryValue));
            }
        } catch (Exception ignored) { }

        return new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entryValue);
    }
}