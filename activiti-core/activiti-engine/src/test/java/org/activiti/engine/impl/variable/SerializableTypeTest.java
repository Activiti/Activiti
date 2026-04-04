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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.naming.Reference;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;
import org.junit.Test;

public class SerializableTypeTest {

    private SerializableType serializableType = new SerializableType();

    @Test
    public void deserialize_should_allowJavaLangTypes() {
        String original = "test string value";
        byte[] bytes = serializeObject(original);
        VariableInstanceEntityImpl valueFields = new VariableInstanceEntityImpl();
        valueFields.setName("testVar");

        Object result = serializableType.deserialize(bytes, valueFields);

        assertThat(result).isEqualTo(original);
    }

    @Test
    public void deserialize_should_allowJavaUtilTypes() {
        HashMap<String, String> original = new HashMap<>();
        original.put("key", "value");
        byte[] bytes = serializeObject(original);
        VariableInstanceEntityImpl valueFields = new VariableInstanceEntityImpl();
        valueFields.setName("testVar");

        Object result = serializableType.deserialize(bytes, valueFields);

        assertThat(result).isEqualTo(original);
    }

    @Test
    public void deserialize_should_allowJavaUtilCollectionTypes() {
        ArrayList<String> original = new ArrayList<>();
        original.add("item1");
        byte[] bytes = serializeObject(original);
        VariableInstanceEntityImpl valueFields = new VariableInstanceEntityImpl();
        valueFields.setName("testVar");

        Object result = serializableType.deserialize(bytes, valueFields);

        assertThat(result).isEqualTo(original);
    }

    @Test
    public void deserialize_should_rejectNonWhitelistedClasses() {
        Reference malicious = new Reference("exploit");
        byte[] bytes = serializeObject(malicious);
        VariableInstanceEntityImpl valueFields = new VariableInstanceEntityImpl();
        valueFields.setName("testVar");

        assertThatThrownBy(() -> serializableType.deserialize(bytes, valueFields))
            .isInstanceOf(ActivitiException.class)
            .hasCauseInstanceOf(InvalidClassException.class);
    }

    @Test
    public void deserialize_should_allowCustomPatternsWhenConfigured() {
        SerializableType customType = new SerializableType(false, List.of(
            "java.lang.**",
            "java.util.**",
            "javax.naming.**"
        ));
        Reference ref = new Reference("allowed");
        byte[] bytes = serializeObject(ref);
        VariableInstanceEntityImpl valueFields = new VariableInstanceEntityImpl();
        valueFields.setName("testVar");

        Object result = customType.deserialize(bytes, valueFields);

        assertThat(result).isInstanceOf(Reference.class);
    }

    @Test
    public void deserialize_should_handleArrayTypes() {
        int[] original = {1, 2, 3};
        byte[] bytes = serializeObject(original);
        VariableInstanceEntityImpl valueFields = new VariableInstanceEntityImpl();
        valueFields.setName("testVar");

        Object result = serializableType.deserialize(bytes, valueFields);

        assertThat(result).isEqualTo(original);
    }

    private byte[] serializeObject(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
