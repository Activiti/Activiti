/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.api.runtime.model.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

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
        for (Map.Entry<String, Object> entry: processVariablesMap.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if(value != null) {
                Class<?> entryValueClass = entry.getValue()
                                                .getClass();

                String entryType;

                if (conversionService.canConvert(entryValueClass, String.class)) {
                    if (Map.class.isInstance(value)) {
                        entryType = Map.class.getName();
                    }
                    else if (JsonNode.class.isInstance(value)) {
                        entryType = JsonNode.class.getName();
                    }
                    else {
                        entryType = entryValueClass.getName();
                    }
                }
                else {
                    entryType = Map.class.getName();
                    value = objectMapper.writeValueAsString(value);
                }


                String entryValue = conversionService.convert(value,
                                                       String.class);

                ProcessVariableValue variableValue = new ProcessVariableValue(entryType,
                                                                              entryValue);
                map.put(name,
                        variableValue);

            } else {
                map.put(name,
                        null);
            }

        }

        gen.writeObject(map);
    }
}