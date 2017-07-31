/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.audit.converter;

import java.io.IOException;
import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.ActivitiException;

public class JpaJsonConverter<T> implements AttributeConverter<T, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private Class<T> entityClass;

    public JpaJsonConverter(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public String convertToDatabaseColumn(T entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new ActivitiException("Unable to serialize object.",
                                        e);
        }
    }

    @Override
    public T convertToEntityAttribute(String entityTextRepresentation) {
        try {
            return objectMapper.readValue(entityTextRepresentation,
                                          entityClass);
        } catch (IOException e) {
            throw new ActivitiException("Unable to deserialize object.",
                                        e);
        }
    }
}