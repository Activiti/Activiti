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

import org.activiti.api.task.model.TaskCandidateUser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class APITaskCandidateUserConverterTest {

    private APITaskCandidateUserConverter taskCandidateUserConverter = new APITaskCandidateUserConverter();

    @Test
    public void fromShouldConvertEngineObjectToModelObject() {
        //given
        org.activiti.engine.task.IdentityLink identityLink = mock(org.activiti.engine.task.IdentityLink.class);
        TaskCandidateUser taskCandidateUser = taskCandidateUserConverter.from(identityLink);

        given(identityLink.getUserId()).willReturn("userId");
        given(identityLink.getTaskId()).willReturn("taskId");

        assertThat(taskCandidateUser).isNotNull();
        assertThat(taskCandidateUser.getUserId()).isNotEqualToIgnoringCase("userId");
        assertThat(taskCandidateUser.getTaskId()).isNotEqualToIgnoringCase("taskId");
    }

}
