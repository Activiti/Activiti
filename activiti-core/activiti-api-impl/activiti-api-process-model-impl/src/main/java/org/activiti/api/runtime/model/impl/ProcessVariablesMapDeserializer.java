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

import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ProcessVariablesMapDeserializer extends JsonDeserializer<ProcessVariablesMap<String, Object>> {

    private final ConversionService conversionService;

    public ProcessVariablesMapDeserializer(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public ProcessVariablesMap<String, Object> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
                                                                                                              JsonProcessingException {
        ProcessVariablesMap<String, Object> map = new ProcessVariablesMap<>();

        JsonNode node = jp.getCodec().readTree(jp);

        node.fields().forEachRemaining(entry -> {
            String name = entry.getKey();
            JsonNode entryValue = entry.getValue();

            if(!entryValue.isNull()) {
                String type = entryValue.get("type").textValue();
                String value = entryValue.get("value").asText();

                Class<?> clazz = ProcessVariablesMapTypeRegistry.forType(type);
                Object result = conversionService.convert(value, clazz);

                map.put(name, result);
            } else {
                map.put(name, null);
            }
        });

        return map;
    }
}