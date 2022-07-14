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

public class ProcessCandidateStarterEventConverterHelperTest {

    private ProcessCandidateStarterEventConverterHelper processCandidateStarterEventConverterHelper = new ProcessCandidateStarterEventConverterHelper();

    @Test
    public void isProcessCandidateStarterUserLink_when_processDefId_userId_and_typeCandidate() {
        IdentityLink identityLink = createUserIdentityLink("aProcessDefId", "aUserId", IdentityLinkType.CANDIDATE);
        assertThat(processCandidateStarterEventConverterHelper.isProcessCandidateStarterUserLink(identityLink)).isTrue();
    }

    @Test
    public void isNotProcessCandidateStarterUserLink_when_processDefId_isNull() {
        IdentityLink identityLink = createUserIdentityLink(null, "aUserId", IdentityLinkType.CANDIDATE);
        assertThat(processCandidateStarterEventConverterHelper.isProcessCandidateStarterUserLink(identityLink)).isFalse();
    }

    @Test
    public void isNotProcessCandidateStarterUserLink_when_userId_isNull() {
        IdentityLink identityLink = createUserIdentityLink("aProcessDefId", null, IdentityLinkType.CANDIDATE);
        assertThat(processCandidateStarterEventConverterHelper.isProcessCandidateStarterUserLink(identityLink)).isFalse();
    }

    @Test
    public void isNotProcessCandidateStarterUserLink_when_type_is_notCandidate() {
        IdentityLink identityLink = createUserIdentityLink("aProcessDefId", "aUserId", IdentityLinkType.PARTICIPANT);
        assertThat(processCandidateStarterEventConverterHelper.isProcessCandidateStarterUserLink(identityLink)).isFalse();
    }

    @Test
    public void isProcessCandidateStarterGroupLink_when_processDefId_groupId_and_typeCandidate() {
        IdentityLink identityLink = createGroupIdentityLink("aProcessDefId", "aGroupId", IdentityLinkType.CANDIDATE);
        assertThat(processCandidateStarterEventConverterHelper.isProcessCandidateStarterGroupLink(identityLink)).isTrue();
    }

    @Test
    public void isNotProcessCandidateStarterGroupLink_when_processDefId_isNull() {
        IdentityLink identityLink = createGroupIdentityLink(null,"aGroupId", IdentityLinkType.CANDIDATE);
        assertThat(processCandidateStarterEventConverterHelper.isProcessCandidateStarterGroupLink(identityLink)).isFalse();
    }

    @Test
    public void isNotProcessCandidateStarterGroupLink_when_groupId_isNull() {
        IdentityLink identityLink = createGroupIdentityLink("aProcessDefId",null, IdentityLinkType.CANDIDATE);
        assertThat(processCandidateStarterEventConverterHelper.isProcessCandidateStarterGroupLink(identityLink)).isFalse();
    }

    @Test
    public void isNotProcessCandidateStarterGroupLink_when_type_is_notCandidate() {
        IdentityLink identityLink = createUserIdentityLink("aProcessDefId", "aUserId", IdentityLinkType.PARTICIPANT);
        assertThat(processCandidateStarterEventConverterHelper.isProcessCandidateStarterGroupLink(identityLink)).isFalse();
    }

    private IdentityLinkEntityImpl createUserIdentityLink(String processDefinitionId, String userId, String type) {
        return createIdentityLink(processDefinitionId, userId, null, type);
    }

    private IdentityLinkEntityImpl createGroupIdentityLink(String processDefinitionId, String groupId, String type) {
        return createIdentityLink(processDefinitionId, null, groupId, type);
    }

    private IdentityLinkEntityImpl createIdentityLink(String processDefinitionId, String userId, String groupId, String type) {
        IdentityLinkEntityImpl identityLink = new IdentityLinkEntityImpl();
        identityLink.setProcessDefId(processDefinitionId);
        identityLink.setUserId(userId);
        identityLink.setGroupId(groupId);
        identityLink.setType(type);
        return identityLink;
    }

}
