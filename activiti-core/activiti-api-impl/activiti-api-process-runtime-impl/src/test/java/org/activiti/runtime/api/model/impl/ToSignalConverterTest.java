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

package org.activiti.runtime.api.model.impl;

import org.activiti.api.process.model.BPMNSignal;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiSignalEventImpl;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class ToSignalConverterTest {

    private ToSignalConverter toSignalConverter = new ToSignalConverter();

    @Test
    public void fromShouldSetMetaInfoAndVariables() {
        //given
        ActivitiSignalEventImpl internalEvent = new ActivitiSignalEventImpl(ActivitiEventType.ACTIVITY_SIGNALED);
        internalEvent.setSignalName("go");
        internalEvent.setSignalData(singletonMap("signalVar", "value"));
        internalEvent.setProcessDefinitionId("procDefId");
        internalEvent.setProcessInstanceId("procInstId");


        //when
        BPMNSignal signal = toSignalConverter.from(internalEvent);

        //then
        assertThat(signal.getSignalPayload().getName()).isEqualTo("go");
        assertThat(signal.getSignalPayload().getVariables()).containsEntry("signalVar", "value");
        assertThat(signal.getProcessDefinitionId()).isEqualTo("procDefId");
        assertThat(signal.getProcessInstanceId()).isEqualTo("procInstId");

    }
}
