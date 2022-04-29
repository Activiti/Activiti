/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.runtime.api.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;
import org.activiti.engine.impl.variable.StringType;
import org.junit.jupiter.api.Test;

class APIVariableInstanceConverterTest {

    private APIVariableInstanceConverter converter = new APIVariableInstanceConverter();

    @Test
    void should_convertToApiVariableInstance() {
        VariableInstanceEntityImpl internalEvent = new VariableInstanceEntityImpl();
        internalEvent.setName("name");
        internalEvent.setType(new StringType(100));
        internalEvent.setValue("someValue");
        internalEvent.setProcessInstanceId("processInstanceId");
        internalEvent.setTaskId("taskId");

        VariableInstance result = converter.from(internalEvent);

        assertThat(result.getName()).isEqualTo("name");
        assertThat(result.getType()).isEqualTo("string");
        assertThat(result.getProcessInstanceId()).isEqualTo("processInstanceId");
        assertThat(result.getTaskId()).isEqualTo("taskId");
        String actualValue = result.getValue();
        assertThat(actualValue).isEqualTo("someValue");
    }
}
