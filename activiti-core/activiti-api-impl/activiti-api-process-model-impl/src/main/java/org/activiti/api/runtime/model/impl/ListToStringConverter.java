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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.core.convert.converter.Converter;

@ProcessVariableTypeConverter
public class ListToStringConverter implements Converter<List<Object>, String> {

    private final ObjectMapper objectMapper;

    public ListToStringConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convert(List<Object> source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }
    }
}
