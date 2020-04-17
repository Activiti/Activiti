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

package org.activiti.runtime.api.event.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.activiti.api.runtime.model.impl.BPMNErrorImpl;
import org.activiti.engine.delegate.event.ActivitiErrorEvent;
import org.junit.jupiter.api.Test;

public class BPMNErrorConverterTest {

    private BPMNErrorConverter bpmnErrorConverter = new BPMNErrorConverter();

    @Test
    public void convertShouldReturnBPMNError() {

        ActivitiErrorEvent internalEvent = mock(ActivitiErrorEvent.class);
        given(internalEvent.getErrorId()).willReturn("errorId");
        given(internalEvent.getErrorCode()).willReturn("errorCode");
        given(internalEvent.getActivityName()).willReturn("activityName");
        given(internalEvent.getActivityType()).willReturn("activityType");
        given(internalEvent.getProcessDefinitionId()).willReturn("procDefId");
        given(internalEvent.getProcessInstanceId()).willReturn("procInstId");

        BPMNErrorImpl bpmnError = bpmnErrorConverter.convertToBPMNError(internalEvent);

        //then
        assertThat(bpmnError).isNotNull();
        assertThat(bpmnError.getProcessInstanceId()).isEqualTo("procInstId");
        assertThat(bpmnError.getProcessDefinitionId()).isEqualTo("procDefId");
        assertThat(bpmnError.getErrorId()).isEqualTo("errorId");
        assertThat(bpmnError.getErrorCode()).isEqualTo("errorCode");
        assertThat(bpmnError.getActivityName()).isEqualTo("activityName");
        assertThat(bpmnError.getActivityType()).isEqualTo("activityType");
    }

}
