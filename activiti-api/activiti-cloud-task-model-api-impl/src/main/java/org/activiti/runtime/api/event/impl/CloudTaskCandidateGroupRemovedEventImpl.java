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

import org.activiti.runtime.api.event.CloudTaskCandidateGroupRemovedEvent;
import org.activiti.runtime.api.event.TaskCandidateGroupEvent;
import org.activiti.runtime.api.model.TaskCandidateGroup;

public class CloudTaskCandidateGroupRemovedEventImpl extends CloudRuntimeEventImpl<TaskCandidateGroup, TaskCandidateGroupEvent.TaskCandidateGroupEvents>
        implements CloudTaskCandidateGroupRemovedEvent {

    public CloudTaskCandidateGroupRemovedEventImpl() {
    }

    public CloudTaskCandidateGroupRemovedEventImpl(TaskCandidateGroup taskCandidateGroup) {
        super(taskCandidateGroup);
        setEntityId(taskCandidateGroup.getGroupId());
    }

    public CloudTaskCandidateGroupRemovedEventImpl(String id,
                                                   Long timestamp,
                                                   TaskCandidateGroup taskCandidateGroup) {
        super(id,
              timestamp,
              taskCandidateGroup);
        setEntityId(taskCandidateGroup.getGroupId());
    }

    @Override
    public TaskCandidateGroupEvents getEventType() {
        return TaskCandidateGroupEvents.TASK_CANDIDATE_GROUP_REMOVED;
    }
}
