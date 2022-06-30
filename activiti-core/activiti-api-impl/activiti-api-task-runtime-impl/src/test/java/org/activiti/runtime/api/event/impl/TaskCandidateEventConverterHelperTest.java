/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityImpl;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskCandidateEventConverterHelperTest {

    private TaskCandidateEventConverterHelper taskCandidateEventConverterHelper = new TaskCandidateEventConverterHelper();

    @Test
    public void isTaskCandidateUserLink_when_taskId_userId_and_typeCandidate() {
        IdentityLink identityLink = createUserIdentityLink("aTaskId", "aUserId", IdentityLinkType.CANDIDATE);
        assertThat(taskCandidateEventConverterHelper.isTaskCandidateUserLink(identityLink)).isTrue();
    }

    @Test
    public void isNotTaskCandidateUserLink_when_taskId_isNull() {
        IdentityLink identityLink = createUserIdentityLink(null, "aUserId", IdentityLinkType.CANDIDATE);
        assertThat(taskCandidateEventConverterHelper.isTaskCandidateUserLink(identityLink)).isFalse();
    }

    @Test
    public void isNotTaskCandidateUserLink_when_userId_isNull() {
        IdentityLink identityLink = createUserIdentityLink("aTaskId", null, IdentityLinkType.CANDIDATE);
        assertThat(taskCandidateEventConverterHelper.isTaskCandidateUserLink(identityLink)).isFalse();
    }

    @Test
    public void isNotTaskCandidateUserLink_when_type_is_notCandidate() {
        IdentityLink identityLink = createUserIdentityLink("aTaskId", "aUserId", IdentityLinkType.PARTICIPANT);
        assertThat(taskCandidateEventConverterHelper.isTaskCandidateUserLink(identityLink)).isFalse();
    }

    @Test
    public void isTaskCandidateGroupLink_when_taskId_groupId_and_typeCandidate() {
        IdentityLink identityLink = createGroupIdentityLink("aTaskId", "aGroupId", IdentityLinkType.CANDIDATE);
        assertThat(taskCandidateEventConverterHelper.isTaskCandidateGroupLink(identityLink)).isTrue();
    }

    @Test
    public void isNotTaskCandidateGroupLink_when_taskId_isNull() {
        IdentityLink identityLink = createGroupIdentityLink(null,"aGroupId", IdentityLinkType.CANDIDATE);
        assertThat(taskCandidateEventConverterHelper.isTaskCandidateGroupLink(identityLink)).isFalse();
    }

    @Test
    public void isNotTaskCandidateGroupLink_when_groupId_isNull() {
        IdentityLink identityLink = createGroupIdentityLink("aTaskId",null, IdentityLinkType.CANDIDATE);
        assertThat(taskCandidateEventConverterHelper.isTaskCandidateGroupLink(identityLink)).isFalse();
    }

    @Test
    public void isNotTaskCandidateGroupLink_when_type_is_notCandidate() {
        IdentityLink identityLink = createUserIdentityLink("aTaskId", "aUserId", IdentityLinkType.PARTICIPANT);
        assertThat(taskCandidateEventConverterHelper.isTaskCandidateGroupLink(identityLink)).isFalse();
    }

    private IdentityLinkEntityImpl createUserIdentityLink(String taskId, String userId, String type) {
        return createIdentityLink(taskId, userId, null, type);
    }

    private IdentityLinkEntityImpl createGroupIdentityLink(String taskId, String groupId, String type) {
        return createIdentityLink(taskId, null, groupId, type);
    }

    private IdentityLinkEntityImpl createIdentityLink(String taskId, String userId, String groupId, String type) {
        IdentityLinkEntityImpl identityLink = new IdentityLinkEntityImpl();
        identityLink.setTaskId(taskId);
        identityLink.setUserId(userId);
        identityLink.setGroupId(groupId);
        identityLink.setType(type);
        return identityLink;
    }
}
