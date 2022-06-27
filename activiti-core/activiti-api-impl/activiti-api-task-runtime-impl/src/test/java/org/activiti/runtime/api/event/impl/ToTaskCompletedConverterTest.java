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
package org.activiti.runtime.api.event.impl;

import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.api.task.runtime.events.TaskCompletedEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.task.Task;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ToTaskCompletedConverterTest {

    @InjectMocks
    private ToTaskCompletedConverter toTaskCompletedConverter;

    @Mock
    private APITaskConverter taskConverter;

    @Mock
    private SecurityManager securityManager;

    @Test
    public void fromShouldReturnAPIEventContainingConvertedTask() {
        //given
        Task internalTask = mock(Task.class);
        org.activiti.api.task.model.Task apiTask = mock(org.activiti.api.task.model.Task.class);
        String loginUser="hruser";
        given(securityManager.getAuthenticatedUserId()).willReturn(loginUser);
        given(taskConverter.fromWithCompletedBy(internalTask, org.activiti.api.task.model.Task.TaskStatus.COMPLETED, loginUser)).willReturn(apiTask);

        ActivitiEntityEvent internalEvent = mock(ActivitiEntityEvent.class);
        given(internalEvent.getEntity()).willReturn(internalTask);

        //when

        TaskCompletedEvent taskCompletedEvent = toTaskCompletedConverter.from(internalEvent).orElse(null);

        //then
        assertThat(taskCompletedEvent).isNotNull();
        assertThat(taskCompletedEvent.getEntity()).isEqualTo(apiTask);
    }

}
