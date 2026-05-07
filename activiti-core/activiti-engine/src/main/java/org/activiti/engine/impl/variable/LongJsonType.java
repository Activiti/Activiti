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
package org.activiti.engine.impl.variable;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import org.activiti.engine.ActivitiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;

public class LongJsonType extends SerializableType {

    private static final Logger logger = LoggerFactory.getLogger(LongJsonType.class);
    public static final String LONG_JSON = "longJson";

    private final int minLength;
    private JsonMapper jsonMapper;
    private boolean serializePOJOsInVariablesToJson;
    private JsonTypeConverter jsonTypeConverter;

    public LongJsonType(
        int minLength,
        JsonMapper jsonMapper,
        boolean serializePOJOsInVariablesToJson,
        JsonTypeConverter jsonTypeConverter
    ) {
        this.minLength = minLength;
        this.jsonMapper = jsonMapper;
        this.serializePOJOsInVariablesToJson = serializePOJOsInVariablesToJson;
        this.jsonTypeConverter = jsonTypeConverter;
    }

    public String getTypeName() {
        return LONG_JSON;
    }

    public boolean isAbleToStore(Object value) {
        if (value == null) {
            return true;
        }

        if (JsonNode.class.isAssignableFrom(value.getClass()) || serializePOJOsInVariablesToJson) {
            try {
                return jsonMapper.writeValueAsString(value).length() >= minLength;
            } catch (JacksonException e) {
                logger.error("Error writing json variable of type " + value.getClass(), e);
            }
        }

        return false;
    }

    public byte[] serialize(Object value, ValueFields valueFields) {
        if (value == null) {
            return null;
        }
        String json = null;
        try {
            json = jsonMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            logger.error("Error writing long json variable " + valueFields.getName(), e);
        }
        try {
            valueFields.setTextValue2(value.getClass().getName());
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ActivitiException("Error getting bytes from json variable", e);
        }
    }

    public Object deserialize(byte[] bytes, ValueFields valueFields) {
        Object jsonValue = null;
        try {
            jsonValue = jsonTypeConverter.convertToValue(jsonMapper.readTree(bytes), valueFields);
        } catch (Exception e) {
            logger.error("Error reading json variable " + valueFields.getName(), e);
        }
        return jsonValue;
    }
}
