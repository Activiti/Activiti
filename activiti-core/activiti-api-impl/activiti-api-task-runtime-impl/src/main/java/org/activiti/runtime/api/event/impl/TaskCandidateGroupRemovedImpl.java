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

import org.activiti.api.runtime.event.impl.RuntimeEventImpl;
import org.activiti.api.task.model.TaskCandidateGroup;
import org.activiti.api.task.model.events.TaskCandidateGroupEvent;
import org.activiti.api.task.runtime.events.TaskCandidateGroupRemovedEvent;

public class TaskCandidateGroupRemovedImpl extends RuntimeEventImpl<TaskCandidateGroup, TaskCandidateGroupEvent.TaskCandidateGroupEvents>
        implements TaskCandidateGroupRemovedEvent {

    public TaskCandidateGroupRemovedImpl() {
    }

    public TaskCandidateGroupRemovedImpl(TaskCandidateGroup entity) {
        super(entity);
    }

    @Override
    public TaskCandidateGroupEvent.TaskCandidateGroupEvents getEventType() {
        return TaskCandidateGroupEvent.TaskCandidateGroupEvents.TASK_CANDIDATE_GROUP_REMOVED;
    }
}
