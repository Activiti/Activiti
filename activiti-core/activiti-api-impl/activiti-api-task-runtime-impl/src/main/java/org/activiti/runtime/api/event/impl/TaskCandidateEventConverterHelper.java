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

import org.activiti.engine.task.IdentityLink;

import static org.activiti.engine.task.IdentityLinkType.CANDIDATE;

public class TaskCandidateEventConverterHelper {

    public boolean isTaskCandidateUserLink(IdentityLink identityLink) {
        return isTaskCandidateLink(identityLink) &&
                identityLink.getUserId() != null;
    }

    public boolean isTaskCandidateGroupLink(IdentityLink identityLink) {
        return isTaskCandidateLink(identityLink) &&
                identityLink.getGroupId() != null;
    }

    private boolean isTaskCandidateLink(IdentityLink identityLink) {
        return identityLink.getTaskId() != null &&
                CANDIDATE.equalsIgnoreCase(identityLink.getType());
    }
}
