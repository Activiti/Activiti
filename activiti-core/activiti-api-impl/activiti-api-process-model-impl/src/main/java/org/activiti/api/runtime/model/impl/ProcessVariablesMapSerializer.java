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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.convert.ConversionService;

public class ProcessVariablesMapSerializer extends StdSerializer<ProcessVariablesMap<String, Object>> {

    private static final long serialVersionUID = 1L;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final ConversionService conversionService;

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
            String entryType = resolveEntryType(value, canConvert);
            String entryValue = conversionService.convert(value, String.class);

            variableValue = new ProcessVariableValue(entryType, entryValue);
        }
        return variableValue;
    }

    private String resolveEntryType(Object value, boolean canConvert) {
        String entryType;
        if (canConvert) {
            if (Map.class.isInstance(value)) {
                entryType = Map.class.getName();
            } else if (JsonNode.class.isInstance(value)) {
                entryType = JsonNode.class.getName();
            } else {
                entryType = value.getClass().getName();
            }
        } else {
            entryType = Map.class.getName();
        }
        return entryType;
    }
}
