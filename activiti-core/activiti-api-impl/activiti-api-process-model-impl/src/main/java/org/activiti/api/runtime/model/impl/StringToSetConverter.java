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

import java.util.Set;

import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

@ProcessVariableTypeConverter
public class StringToSetConverter implements Converter<String, Set<Object>> {
    private final ObjectMapper objectMapper;

    public StringToSetConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Set<Object> convert(String source) {
        JavaType javaType = objectMapper.getTypeFactory()
                                        .constructParametricType(Set.class,
                                                                 Object.class);
        try {
            return objectMapper.readValue(source, javaType);
        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }
    }
}
