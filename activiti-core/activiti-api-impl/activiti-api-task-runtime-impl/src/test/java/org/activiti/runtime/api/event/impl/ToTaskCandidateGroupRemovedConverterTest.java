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
package org.activiti.runtime.api.event.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.activiti.api.task.model.TaskCandidateGroup;
import org.activiti.api.task.model.impl.TaskCandidateGroupImpl;
import org.activiti.api.task.runtime.events.TaskCandidateGroupRemovedEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.runtime.api.model.impl.APITaskCandidateGroupConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToTaskCandidateGroupRemovedConverterTest {

    @InjectMocks
    private ToTaskCandidateGroupRemovedConverter converter;

    @Mock
    private APITaskCandidateGroupConverter candidateConverter;

    @Test
    void from_should_propagateProcessInstanceAndDefinitionIds_when_taskBelongsToProcessInstance() {
        IdentityLink identityLink = mock(IdentityLink.class);
        given(identityLink.getType()).willReturn(IdentityLinkType.CANDIDATE);
        given(identityLink.getGroupId()).willReturn("group1");
        given(identityLink.getTaskId()).willReturn("task1");

        ActivitiEntityEvent internalEvent = mock(ActivitiEntityEvent.class);
        given(internalEvent.getEntity()).willReturn(identityLink);
        given(internalEvent.getProcessInstanceId()).willReturn("pid-42");
        given(internalEvent.getProcessDefinitionId()).willReturn("pdef-1");

        TaskCandidateGroup candidate = new TaskCandidateGroupImpl("group1", "task1");
        given(candidateConverter.from(identityLink)).willReturn(candidate);

        TaskCandidateGroupRemovedEvent event = converter.from(internalEvent).orElse(null);

        assertThat(event).isNotNull();
        assertThat(event.getEntity()).isEqualTo(candidate);
        assertThat(event.getProcessInstanceId()).isEqualTo("pid-42");
        assertThat(event.getProcessDefinitionId()).isEqualTo("pdef-1");
    }

    @Test
    void from_should_leaveProcessInstanceIdNull_when_taskIsStandalone() {
        IdentityLink identityLink = mock(IdentityLink.class);
        given(identityLink.getType()).willReturn(IdentityLinkType.CANDIDATE);
        given(identityLink.getGroupId()).willReturn("group1");
        given(identityLink.getTaskId()).willReturn("task1");

        ActivitiEntityEvent internalEvent = mock(ActivitiEntityEvent.class);
        given(internalEvent.getEntity()).willReturn(identityLink);
        given(internalEvent.getProcessInstanceId()).willReturn(null);
        given(internalEvent.getProcessDefinitionId()).willReturn(null);

        TaskCandidateGroup candidate = new TaskCandidateGroupImpl("group1", "task1");
        given(candidateConverter.from(identityLink)).willReturn(candidate);

        TaskCandidateGroupRemovedEvent event = converter.from(internalEvent).orElse(null);

        assertThat(event).isNotNull();
        assertThat(event.getEntity()).isEqualTo(candidate);
        assertThat(event.getProcessInstanceId()).isNull();
        assertThat(event.getProcessDefinitionId()).isNull();
    }

    @Test
    void from_should_returnEmpty_when_entityIsNotIdentityLink() {
        ActivitiEntityEvent internalEvent = mock(ActivitiEntityEvent.class);
        given(internalEvent.getEntity()).willReturn(new Object());

        assertThat(converter.from(internalEvent)).isEmpty();
    }
}
