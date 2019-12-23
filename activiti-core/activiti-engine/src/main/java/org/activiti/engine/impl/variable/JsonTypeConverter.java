/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.engine.impl.variable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonTypeConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonType.class);

    private ObjectMapper objectMapper;
    private String javaClassFieldForJackson;

    public JsonTypeConverter(ObjectMapper objectMapper, String javaClassFieldForJackson) {
        this.objectMapper = objectMapper;
        this.javaClassFieldForJackson = javaClassFieldForJackson;
    }

    public Object convertToValue(JsonNode jsonValue, ValueFields valueFields) {
        Object convertedValue = jsonValue;
        if (jsonValue != null && StringUtils.isNotBlank(javaClassFieldForJackson)) {
            //can find type so long as JsonTypeInfo annotation on the class - see https://stackoverflow.com/a/28384407/9705485
            JsonNode classNode = jsonValue.get(javaClassFieldForJackson);
            try {
                if (classNode != null) {
                    final String type = classNode.asText();
                    convertedValue = convertToType(jsonValue, type);
                } else if (valueFields.getTextValue2() != null &&
                    !jsonValue.getClass().getName().equals(valueFields.getTextValue2())) {
                    convertedValue = convertToType(jsonValue, valueFields.getTextValue2());
                }
            } catch (ClassNotFoundException e) {
                LOGGER
                    .warn("Unable to obtain type for json variable object " + valueFields.getName(),
                        e);
            }
        }

        return convertedValue;
    }

    private Object convertToType(JsonNode jsonValue, String type) throws ClassNotFoundException {
        return objectMapper.convertValue(jsonValue, loadClass(type));
    }

    private Class<?> loadClass(String type) throws ClassNotFoundException {
        return Class.forName(type, false, this.getClass().getClassLoader());
    }

}
