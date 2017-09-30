/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.audit.mongo.events;

import org.activiti.services.audit.mongo.events.model.Task;

public class TaskAssignedEventDocument extends ProcessEngineEventDocument {

    protected static final String TASK_ASSIGNED_EVENT = "TaskAssignedEvent";

    private Task task;

    public Task getTask() {
        return task;
    }
}
