/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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
package org.activiti.runtime.api.event.impl;

import static org.activiti.runtime.api.event.impl.VariableBuilder.buildTaskVariableEvent;
import static org.activiti.runtime.api.event.impl.VariableBuilder.buildVariableEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.activiti.engine.delegate.event.impl.ActivitiVariableEventImpl;
import org.activiti.spring.process.ProcessExtensionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EphemeralVariableResolverTest {

    @Mock
    private ProcessExtensionService processExtensionService;

    @InjectMocks
    private EphemeralVariableResolver resolver;

    @Test
    void should_considerEphemeral_when_hasEphemeralVariableWithSameNameAndItsNotAUserTask() {
        //given
        ActivitiVariableEventImpl internalEvent = buildVariableEvent();
        given(
            processExtensionService.hasEphemeralVariable(
                internalEvent.getProcessDefinitionId(),
                internalEvent.getVariableName()
            )
        ).willReturn(true);

        //when
        assertThat(resolver.isEphemeralVariable(internalEvent)).isTrue();
    }

    @Test
    void should_notConsiderEphemeral_when_dontHaveEphemeralVariableWithSameName() {
        //given
        ActivitiVariableEventImpl internalEvent = buildVariableEvent();
        given(
            processExtensionService.hasEphemeralVariable(
                internalEvent.getProcessDefinitionId(),
                internalEvent.getVariableName()
            )
        ).willReturn(false);

        //when
        assertThat(resolver.isEphemeralVariable(internalEvent)).isFalse();
    }

    @Test
    void should_notConsiderEphemeral_when_itsTaskVariable() {
        //given
        ActivitiVariableEventImpl internalEvent = buildTaskVariableEvent();

        //when
        verify(processExtensionService, never()).hasEphemeralVariable(any(), any());
        assertThat(resolver.isEphemeralVariable(internalEvent)).isFalse();
    }
}
