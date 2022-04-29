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

import org.activiti.api.runtime.event.impl.RuntimeEventImpl;
import org.activiti.api.task.model.TaskCandidateUser;
import org.activiti.api.task.model.events.TaskCandidateUserEvent;
import org.activiti.api.task.runtime.events.TaskCandidateUserAddedEvent;

public class TaskCandidateUserAddedEventImpl extends RuntimeEventImpl<TaskCandidateUser, TaskCandidateUserEvent.TaskCandidateUserEvents>
        implements TaskCandidateUserAddedEvent {

    public TaskCandidateUserAddedEventImpl() {
    }

    public TaskCandidateUserAddedEventImpl(TaskCandidateUser entity) {
        super(entity);
    }

    @Override
    public TaskCandidateUserEvent.TaskCandidateUserEvents getEventType() {
        return TaskCandidateUserEvent.TaskCandidateUserEvents.TASK_CANDIDATE_USER_ADDED;
    }

}
