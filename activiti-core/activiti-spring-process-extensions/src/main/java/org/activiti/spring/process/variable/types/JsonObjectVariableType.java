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
package org.activiti.spring.process.variable.types;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import java.util.List;
import org.activiti.engine.ActivitiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonObjectVariableType extends VariableType {

    private static final Logger logger = LoggerFactory.getLogger(JsonObjectVariableType.class);

    private JsonMapper jsonMapper;

    public JsonObjectVariableType(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public JsonMapper getObjectMapper() {
        return jsonMapper;
    }

    public void setObjectMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void validate(Object var, List<ActivitiException> errors) {
        //we can consider var json so long as it can be stored as json
        //this doesn't guarantee a string body to be valid json as jackson will wrap a string to make it valid
        //also doesn't guarantee it will be persisted as json
        //could be a pojo and then could be persisted as serializable if user sets serializePOJOsInVariablesToJson to false - see JsonType.java

        String json = null;
        try {
            json = jsonMapper.writeValueAsString(var);
        } catch (JacksonException e) {
            String message = var.getClass() + " is not serializable as json";
            errors.add(new ActivitiException(message));
            logger.error(message, e);
        }

        if (json != null) {
            try {
                jsonMapper.readValue(json, var.getClass());
            } catch (JacksonException e) {
                String message = var.getClass() + " is not deserializable as json";
                errors.add(new ActivitiException(message));
                logger.error(message, e);
            }
        }
    }
}
