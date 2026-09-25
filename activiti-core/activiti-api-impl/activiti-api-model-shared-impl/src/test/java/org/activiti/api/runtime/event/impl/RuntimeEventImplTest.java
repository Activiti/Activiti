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
package org.activiti.api.runtime.event.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.activiti.api.model.shared.event.VariableEvent.VariableEvents;
import org.activiti.api.runtime.model.impl.VariableInstanceImpl;
import org.junit.jupiter.api.Test;

class RuntimeEventImplTest {

    @Test
    void should_notRenderNestedVariableValueInToString() {
        AtomicInteger toStringInvocations = new AtomicInteger();
        Object value = new Object() {
            @Override
            public String toString() {
                toStringInvocations.incrementAndGet();
                return "sensitive-value";
            }
        };

        VariableInstanceImpl<Object> variable = new VariableInstanceImpl<>(
            "name",
            "type",
            value,
            "processInstanceId",
            "taskId"
        );
        TestRuntimeEvent event = new TestRuntimeEvent(variable);

        String result = event.toString();

        assertThat(toStringInvocations).hasValue(0);
        assertThat(result)
            .contains("entityType=VariableInstanceImpl")
            .doesNotContain("sensitive-value");
    }

    private static class TestRuntimeEvent extends RuntimeEventImpl<VariableInstanceImpl<Object>, VariableEvents> {

        private TestRuntimeEvent(VariableInstanceImpl<Object> entity) {
            super(entity);
        }

        @Override
        public VariableEvents getEventType() {
            return VariableEvents.VARIABLE_CREATED;
        }
    }
}
