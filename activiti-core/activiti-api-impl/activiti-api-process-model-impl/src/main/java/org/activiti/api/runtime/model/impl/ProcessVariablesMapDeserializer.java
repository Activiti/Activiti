/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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

import java.util.function.Supplier;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ValueDeserializer;

public class ProcessVariablesMapDeserializer extends ValueDeserializer<ProcessVariablesMap<String, Object>> {

    private static final Logger logger = LoggerFactory.getLogger(ProcessVariablesMapDeserializer.class);

    private static final String VALUE = "value";
    private static final String TYPE = "type";
    private static final JsonMapper jsonMapper = new JsonMapper();
    private final Supplier<ConversionService> conversionServiceSupplier;

    public ProcessVariablesMapDeserializer(Supplier<ConversionService> conversionServiceSupplier) {
        this.conversionServiceSupplier = conversionServiceSupplier;
    }

    @Override
    public ProcessVariablesMap<String, Object> deserialize(JsonParser jp, DeserializationContext ctxt) {
        ProcessVariablesMap<String, Object> map = new ProcessVariablesMap<>();

        JsonNode node = ctxt.readTree(jp);
        node
            .properties().iterator()
            .forEachRemaining(entry -> {
                String name = entry.getKey();
                JsonNode entryValue = entry.getValue();

                if (!entryValue.isNull()) {
                    if (entryValue.get(TYPE) != null && entryValue.get(VALUE) != null) {
                        String type = entryValue.get(TYPE).asString();
                        String value = entryValue.get(VALUE).asString();

                        Class<?> clazz = ProcessVariablesMapTypeRegistry.forType(type);

                        ConversionService conversionService = conversionServiceSupplier.get();

                        Object result = conversionService.convert(value, clazz);

                        if (ObjectValue.class.isInstance(result)) {
                            result = ObjectValue.class.cast(result).getObject();
                        }
                        map.put(name, result);

                    } else {
                        Object value = null;
                        try {
                            value = jsonMapper.treeToValue(entryValue, Object.class);
                        } catch (JacksonException e) {
                            logger.error("Unexpected Json Processing Exception: ", e);
                        }
                        map.put(name, value);
                    }
                } else {
                    map.put(name, null);
                }
            });

        return map;
    }
}
