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

import org.activiti.api.task.model.TaskCandidateUser;

public class TaskCandidateUserImpl extends TaskCandidateImpl implements TaskCandidateUser {

    private String userId;

    public TaskCandidateUserImpl(){

    }

    public TaskCandidateUserImpl(String userId, String taskId){
        super(taskId);
        this.userId = userId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskCandidateUserImpl that = (TaskCandidateUserImpl) o;
        return Objects.equals(userId,
                              that.userId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(userId);
    }
}
