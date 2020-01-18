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

import org.activiti.runtime.api.event.CloudTaskCandidateUserAddedEvent;
import org.activiti.runtime.api.event.TaskCandidateUserEvent;
import org.activiti.runtime.api.model.TaskCandidateUser;

public class CloudTaskCandidateUserAddedEventImpl extends CloudRuntimeEventImpl<TaskCandidateUser, TaskCandidateUserEvent.TaskCandidateUserEvents>
        implements CloudTaskCandidateUserAddedEvent {

    public CloudTaskCandidateUserAddedEventImpl() {
    }

    public CloudTaskCandidateUserAddedEventImpl(TaskCandidateUser taskCandidateUser) {
        super(taskCandidateUser);
        setEntityId(taskCandidateUser.getUserId());
    }

    public CloudTaskCandidateUserAddedEventImpl(String id,
                                                Long timestamp,
                                                TaskCandidateUser taskCandidateUser) {
        super(id,
              timestamp,
              taskCandidateUser);
        setEntityId(taskCandidateUser.getUserId());
    }

    @Override
    public TaskCandidateUserEvent.TaskCandidateUserEvents getEventType() {
        return TaskCandidateUserEvent.TaskCandidateUserEvents.TASK_CANDIDATE_USER_ADDED;
    }
}
