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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.activiti.engine.impl.variable.store.VariableContentStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExternalStoreVariableTypeTest {

    private VariableContentStore store;
    private ExternalStoreVariableType type;

    @BeforeEach
    void setUp() {
        store = mock(VariableContentStore.class);
        when(store.getStoreName()).thenReturn("test-store");
        type = new ExternalStoreVariableType(store, Arrays.asList(new ByteArrayType(), new SerializableType()));
    }

    @Test
    void getTypeName() {
        assertThat(type.getTypeName()).isEqualTo("externalStore");
    }

    @Test
    void isAbleToStoreByteArray() {
        assertThat(type.isAbleToStore(new byte[] { 1, 2 })).isTrue();
    }

    @Test
    void isAbleToStoreSerializable() {
        assertThat(type.isAbleToStore("a string")).isTrue();
    }

    @Test
    void isAbleToStoreNull() {
        assertThat(type.isAbleToStore(null)).isTrue();
    }

    @Test
    void setValueStoresBytes() {
        when(store.store(any(), any(), any())).thenReturn("content-id-1");
        TestValueFields fields = new TestValueFields("myVar", "proc-1");

        type.setValue("hello world", fields);

        verify(store).store(eq("myVar"), eq("proc-1"), any(byte[].class));
        assertThat(fields.getContentStoreName()).isEqualTo("test-store");
        assertThat(fields.getContentId()).isEqualTo("content-id-1");
    }

    @Test
    void getValueLoadsBytes() {
        byte[] data = "hello world".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(store.load("content-id-1")).thenReturn(data);
        TestValueFields fields = new TestValueFields("myVar", "proc-1");
        fields.setContentId("content-id-1");
        fields.setContentStoreName("test-store");
        fields.setTextValue2(String.class.getName());

        Object value = type.getValue(fields);

        assertThat(value).isEqualTo("hello world");
    }

    @Test
    void getValueNullContentIdReturnsNull() {
        TestValueFields fields = new TestValueFields("myVar", "proc-1");
        assertThat(type.getValue(fields)).isNull();
    }

    static class TestValueFields implements ValueFields {

        private final String name;
        private final String processInstanceId;
        private String textValue;
        private String textValue2;
        private String contentStoreName;
        private String contentId;
        private Long longValue;
        private Double doubleValue;
        private byte[] bytes;
        private Object cachedValue;

        TestValueFields(String name, String processInstanceId) {
            this.name = name;
            this.processInstanceId = processInstanceId;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getProcessInstanceId() {
            return processInstanceId;
        }

        @Override
        public String getExecutionId() {
            return null;
        }

        @Override
        public String getTaskId() {
            return null;
        }

        @Override
        public String getTextValue() {
            return textValue;
        }

        @Override
        public void setTextValue(String textValue) {
            this.textValue = textValue;
        }

        @Override
        public String getTextValue2() {
            return textValue2;
        }

        @Override
        public void setTextValue2(String textValue2) {
            this.textValue2 = textValue2;
        }

        @Override
        public Long getLongValue() {
            return longValue;
        }

        @Override
        public void setLongValue(Long longValue) {
            this.longValue = longValue;
        }

        @Override
        public Double getDoubleValue() {
            return doubleValue;
        }

        @Override
        public void setDoubleValue(Double doubleValue) {
            this.doubleValue = doubleValue;
        }

        @Override
        public byte[] getBytes() {
            return bytes;
        }

        @Override
        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public Object getCachedValue() {
            return cachedValue;
        }

        @Override
        public void setCachedValue(Object cachedValue) {
            this.cachedValue = cachedValue;
        }

        @Override
        public String getContentStoreName() {
            return contentStoreName;
        }

        @Override
        public void setContentStoreName(String contentStoreName) {
            this.contentStoreName = contentStoreName;
        }

        @Override
        public String getContentId() {
            return contentId;
        }

        @Override
        public void setContentId(String contentId) {
            this.contentId = contentId;
        }
    }
}
