/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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
package org.activiti.api.runtime.model.impl;

import java.util.Objects;
import org.activiti.api.model.shared.model.IdentityLink;

public class IdentityLinkImpl implements IdentityLink {

    private String type;
    private String userId;
    private String groupId;
    private String taskId;
    private String processDefinitionId;
    private String processInstanceId;
    private byte[] details;

    public IdentityLinkImpl() {}

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public byte[] getDetails() {
        return details;
    }

    public void setDetails(byte[] details) {
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdentityLinkImpl that = (IdentityLinkImpl) o;
        return Objects.equals(type, that.type) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(groupId, that.groupId) &&
               Objects.equals(taskId, that.taskId) &&
               Objects.equals(processDefinitionId, that.processDefinitionId) &&
               Objects.equals(processInstanceId, that.processInstanceId) &&
               Objects.deepEquals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, userId, groupId, taskId, processDefinitionId, processInstanceId, Objects.hashCode(details));
    }

    @Override
    public String toString() {
        return "IdentityLinkImpl{" +
               "type='" + type + '\'' +
               ", userId='" + userId + '\'' +
               ", groupId='" + groupId + '\'' +
               ", taskId='" + taskId + '\'' +
               ", processDefinitionId='" + processDefinitionId + '\'' +
               ", processInstanceId='" + processInstanceId + '\'' +
               '}';
    }
}
