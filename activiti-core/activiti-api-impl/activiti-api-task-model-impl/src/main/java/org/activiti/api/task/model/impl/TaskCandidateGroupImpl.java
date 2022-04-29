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

import org.activiti.api.task.model.TaskCandidateGroup;

public class TaskCandidateGroupImpl extends TaskCandidateImpl implements TaskCandidateGroup {

    private String groupId;

    public TaskCandidateGroupImpl(){
    }

    public TaskCandidateGroupImpl(String groupId, String taskId){
        super(taskId);
        this.groupId = groupId;
    }


    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskCandidateGroupImpl that = (TaskCandidateGroupImpl) o;
        return Objects.equals(groupId,
                              that.groupId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(groupId);
    }
}
