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

import static org.activiti.api.runtime.model.impl.ProcessVariablesMapTypeRegistry.OBJECT_TYPE_KEY;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.databind.ObjectMapper;

@ProcessVariableTypeConverter
public class ObjectValueToStringConverter implements Converter<ObjectValue, String> {
    private static final String CLASS = "@class";
    private final ObjectMapper objectMapper;

    public ObjectValueToStringConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String convert(ObjectValue source) {

        try {
            Map<String, Object> value = objectMapper.convertValue(source, Map.class);

            if (Map.class.isInstance(value.get(OBJECT_TYPE_KEY))) {
                Map<String, Object> object = objectMapper.convertValue(source.getObject(), Map.class);

                if (object.containsKey(CLASS)) {
                    Map.class.cast(value.get(OBJECT_TYPE_KEY)).put(CLASS, object.get(CLASS));
                }
            }

            return objectMapper.writeValueAsString(value);
        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }
    }
}
