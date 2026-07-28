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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class JsonTypeConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonType.class);

    private JsonMapper jsonMapper;
    private String javaClassFieldForJackson;

    public JsonTypeConverter(JsonMapper jsonMapper, String javaClassFieldForJackson) {
        this.jsonMapper = jsonMapper;
        this.javaClassFieldForJackson = javaClassFieldForJackson;
    }

    public Object convertToValue(JsonNode jsonValue, ValueFields valueFields) {
        Object convertedValue = jsonValue;
        if (jsonValue != null && StringUtils.isNotBlank(javaClassFieldForJackson)) {
            //can find type so long as JsonTypeInfo annotation on the class - see https://stackoverflow.com/a/28384407/9705485
            JsonNode classNode = jsonValue.get(javaClassFieldForJackson);
            try {
                if (classNode != null) {
                    final String type = classNode.asString();
                    convertedValue = convertToType(jsonValue, type);
                } else if (
                    valueFields.getTextValue2() != null &&
                    !jsonValue.getClass().getName().equals(valueFields.getTextValue2())
                ) {
                    convertedValue = convertToType(jsonValue, valueFields.getTextValue2());
                }
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Unable to obtain type for json variable object " + valueFields.getName(), e);
            }
        }

        return convertedValue;
    }

    private Object convertToType(JsonNode jsonValue, String type) throws ClassNotFoundException {
        return jsonMapper.convertValue(jsonValue, loadClass(type));
    }

    private Class<?> loadClass(String type) throws ClassNotFoundException {
        return Class.forName(type, false, this.getClass().getClassLoader());
    }
}
