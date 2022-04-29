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
package org.activiti.api.task.model.impl;

import java.util.Objects;

import org.activiti.api.task.model.TaskCandidate;

public abstract class TaskCandidateImpl implements TaskCandidate {

    private String taskId;

    public TaskCandidateImpl() {
    }

    public TaskCandidateImpl(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskCandidateImpl that = (TaskCandidateImpl) o;
        return Objects.equals(taskId,
                              that.taskId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(taskId);
    }
}
