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

package org.activiti.runtime.api.model.impl;

import org.activiti.api.task.model.TaskCandidateGroup;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class APITaskCandidateGroupConverterTest {

    private APITaskCandidateGroupConverter taskCandidateGroupConverter = new APITaskCandidateGroupConverter();

    @Test
    public void fromShouldConvertEngineObjectToModelObject() {
        //given
        org.activiti.engine.task.IdentityLink identityLink = mock(org.activiti.engine.task.IdentityLink.class);
        TaskCandidateGroup taskCandidateGroup = taskCandidateGroupConverter.from(identityLink);

        given(identityLink.getGroupId()).willReturn("groupId");
        given(identityLink.getTaskId()).willReturn("taskId");

        assertThat(taskCandidateGroup).isNotNull();
        assertThat(taskCandidateGroup.getGroupId()).isNotEqualToIgnoringCase("groupId");
        assertThat(taskCandidateGroup.getTaskId()).isNotEqualToIgnoringCase("taskId");
    }

}
