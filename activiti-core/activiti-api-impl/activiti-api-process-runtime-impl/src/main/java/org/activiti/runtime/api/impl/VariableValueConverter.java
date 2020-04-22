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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.api.process.model.payloads.VariableValue;
import org.activiti.engine.ActivitiException;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;

public class VariableValueConverter  {

    protected static Map<String, Class<?>> typeRegistry = new HashMap<>();

    static {
        typeRegistry.put("string", String.class);
        typeRegistry.put("long", Long.class);
        typeRegistry.put("int", Integer.class);
        typeRegistry.put("integer", Integer.class);
        typeRegistry.put("boolean", Boolean.class);
        typeRegistry.put("double", Double.class);
        typeRegistry.put("Date", Date.class);
        typeRegistry.put("LocalDate", LocalDate.class);
        typeRegistry.put("String", String.class);
        typeRegistry.put("Long", Long.class);
        typeRegistry.put("Integer", Integer.class);
        typeRegistry.put("Boolean", Boolean.class);
        typeRegistry.put("Double", Double.class);
        typeRegistry.put("BigDecimal", BigDecimal.class);
        typeRegistry.put("JsonNode", JsonNode.class);
        typeRegistry.put("Map", Map.class);
    };

    private final ConversionService conversionService;

    public VariableValueConverter() {
        this(ApplicationConversionService.getSharedInstance());
    }

    public VariableValueConverter(ConversionService conversionService) {
        Assert.notNull(conversionService, "ConversionService must not be null");
        this.conversionService = conversionService;
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(VariableValue variableValue) {
        Class<?> type = typeRegistry.getOrDefault(variableValue.getType(), Object.class);
        Object value = variableValue.getValue();

        try {
            return (T) type.cast(this.conversionService.convert(value, type));
        }
        catch (Exception ex) {
            throw new ActivitiException("VariableValue conversion error", ex);
        }
    }

}
