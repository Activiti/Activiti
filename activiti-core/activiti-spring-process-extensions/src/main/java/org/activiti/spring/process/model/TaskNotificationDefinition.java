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
package org.activiti.spring.process.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TaskNotificationDefinition {

    private Map<String, Boolean> assigneeNotifications = new HashMap<>();
    private Map<String, Boolean> candidateUserNotifications = new HashMap<>();
    private Map<String, Boolean> candidateGroupNotifications = new HashMap<>();

    public Map<String, Boolean> getAssigneeNotifications() {
        return assigneeNotifications;
    }

    public void setAssigneeNotifications(Map<String, Boolean> assigneeNotifications) {
        this.assigneeNotifications = assigneeNotifications;
    }

    public Map<String, Boolean> getCandidateUserNotifications() {
        return candidateUserNotifications;
    }

    public void setCandidateUserNotifications(Map<String, Boolean> candidateUserNotifications) {
        this.candidateUserNotifications = candidateUserNotifications;
    }

    public Map<String, Boolean> getCandidateGroupNotifications() {
        return candidateGroupNotifications;
    }

    public void setCandidateGroupNotifications(Map<String, Boolean> candidateGroupNotifications) {
        this.candidateGroupNotifications = candidateGroupNotifications;
    }

    public void setAssigneeNotification(String assigneeId, Boolean enableEmailNotifications) {
        assigneeNotifications.put(assigneeId, enableEmailNotifications);
    }

    public void setCandidateUserNotification(String candidateUserId, Boolean enableEmailNotifications) {
        candidateUserNotifications.put(candidateUserId, enableEmailNotifications);
    }

    public void setCandidateGroupNotification(String candidateGroupId, Boolean enableEmailNotifications) {
        candidateGroupNotifications.put(candidateGroupId, enableEmailNotifications);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskNotificationDefinition that = (TaskNotificationDefinition) o;
        return Objects.equals(assigneeNotifications, that.assigneeNotifications) &&
               Objects.equals(candidateUserNotifications, that.candidateUserNotifications) &&
               Objects.equals(candidateGroupNotifications, that.candidateGroupNotifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assigneeNotifications, candidateUserNotifications, candidateGroupNotifications);
    }

    @Override
    public String toString() {
        return "TaskNotificationDefinition{" +
            "assigneeNotifications=" + assigneeNotifications +
            ", candidateUserNotifications=" + candidateUserNotifications +
            ", candidateGroupNotifications=" + candidateGroupNotifications +
            '}';
    }
}
