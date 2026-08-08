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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.variable.store.VariableContentStore;

/**
 * A VariableType that delegates byte storage to a pluggable {@link VariableContentStore}.
 * The variable row carries only the storeName and contentId as lightweight references.
 */
public class ExternalStoreVariableType implements VariableType {

    public static final String TYPE_NAME = "externalStore";
    private static final String STRING_SERIALIZER = "string";

    private final VariableContentStore store;
    private final List<VariableType> candidateTypes;

    public ExternalStoreVariableType(VariableContentStore store, List<VariableType> candidateTypes) {
        this.store = store;
        this.candidateTypes = candidateTypes;
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    @Override
    public boolean isAbleToStore(Object value) {
        for (VariableType candidateType : candidateTypes) {
            if (candidateType.isAbleToStore(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setValue(Object value, ValueFields fields) {
        String previousContentId = fields.getContentId();
        VariableType candidateType = findCandidateType(value);
        byte[] bytes = serialize(value, fields, candidateType);

        if (bytes == null) {
            fields.setContentStoreName(null);
            fields.setContentId(null);
            if (previousContentId != null) {
                store.delete(previousContentId);
            }
            fields.setCachedValue(null);
            return;
        }

        String contentId = store.store(fields.getName(), fields.getProcessInstanceId(), bytes);
        fields.setContentStoreName(store.getStoreName());
        fields.setContentId(contentId);
        fields.setTextValue(resolveSerializerType(candidateType, value));
        if (value != null && !(candidateType instanceof LongJsonType)) {
            fields.setTextValue2(value.getClass().getName());
        }
        if (previousContentId != null && !previousContentId.equals(contentId)) {
            store.delete(previousContentId);
        }
        fields.setCachedValue(value);
    }

    @Override
    public Object getValue(ValueFields fields) {
        String contentId = fields.getContentId();
        if (contentId == null) {
            return null;
        }

        byte[] bytes = store.load(contentId);
        if (bytes == null) {
            return null;
        }

        return deserialize(bytes, fields);
    }

    private VariableType findCandidateType(Object value) {
        for (VariableType candidateType : candidateTypes) {
            if (candidateType.isAbleToStore(value)) {
                return candidateType;
            }
        }
        throw new ActivitiException("Cannot find candidate type for variable '" + value + "'");
    }

    private byte[] serialize(Object value, ValueFields fields, VariableType candidateType) {
        if (value == null) {
            return null;
        }
        if (value instanceof byte[] bytes) {
            return bytes;
        }
        if (value instanceof String stringValue) {
            return stringValue.getBytes(StandardCharsets.UTF_8);
        }
        if (candidateType instanceof LongJsonType longJsonType) {
            return longJsonType.serialize(value, fields);
        }
        if (candidateType instanceof SerializableType serializableType && value instanceof Serializable) {
            return serializableType.serialize(value, fields);
        }
        if (value instanceof Serializable) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(value);
                return baos.toByteArray();
            } catch (Exception e) {
                throw new ActivitiException("Cannot serialize variable '" + fields.getName() + "'", e);
            }
        }
        throw new ActivitiException(
            "Cannot serialize variable '" + fields.getName() + "': not Serializable"
        );
    }

    private String resolveSerializerType(VariableType candidateType, Object value) {
        if (value instanceof byte[]) {
            return new ByteArrayType().getTypeName();
        }
        if (candidateType instanceof LongJsonType) {
            return candidateType.getTypeName();
        }
        if (value instanceof String) {
            return STRING_SERIALIZER;
        }
        return SerializableType.TYPE_NAME;
    }

    private Object deserialize(byte[] bytes, ValueFields fields) {
        String serializerType = fields.getTextValue();
        if (LongJsonType.LONG_JSON.equals(serializerType)) {
            VariableType candidateType = getCandidateType(serializerType);
            if (candidateType instanceof LongJsonType longJsonType) {
                return longJsonType.deserialize(bytes, fields);
            }
        }
        if (STRING_SERIALIZER.equals(serializerType)) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        if (new ByteArrayType().getTypeName().equals(serializerType)) {
            return bytes;
        }

        String javaType = fields.getTextValue2();
        if (javaType == null) {
            return bytes;
        }
        if ("byte[]".equals(javaType) || "[B".equals(javaType)) {
            return bytes;
        }
        if (String.class.getName().equals(javaType)) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        } catch (Exception e) {
            throw new ActivitiException("Cannot deserialize variable '" + fields.getName() + "'", e);
        }
    }

    private VariableType getCandidateType(String typeName) {
        for (VariableType candidateType : candidateTypes) {
            if (candidateType.getTypeName().equals(typeName)) {
                return candidateType;
            }
        }
        return null;
    }
}
