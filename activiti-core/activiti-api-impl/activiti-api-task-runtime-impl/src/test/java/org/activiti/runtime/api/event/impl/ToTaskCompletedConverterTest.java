/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

import org.activiti.api.task.runtime.events.TaskCompletedEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.task.Task;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class ToTaskCompletedConverterTest {

    @InjectMocks
    private ToTaskCompletedConverter toTaskCompletedConverter;

    @Mock
    private APITaskConverter taskConverter;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void fromShouldReturnAPIEventContainingConvertedTask() {
        //given
        Task internalTask = mock(Task.class);
        org.activiti.api.task.model.Task apiTask = mock(org.activiti.api.task.model.Task.class);
        given(taskConverter.from(internalTask, org.activiti.api.task.model.Task.TaskStatus.COMPLETED)).willReturn(apiTask);

        ActivitiEntityEvent internalEvent = mock(ActivitiEntityEvent.class);
        given(internalEvent.getEntity()).willReturn(internalTask);

        //when
        TaskCompletedEvent taskCompletedEvent = toTaskCompletedConverter.from(internalEvent).orElse(null);

        //then
        assertThat(taskCompletedEvent).isNotNull();
        assertThat(taskCompletedEvent.getEntity()).isEqualTo(apiTask);
    }

}
