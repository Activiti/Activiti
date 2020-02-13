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

package org.activiti.api.task.model;

import org.activiti.api.model.shared.model.ApplicationElement;

import java.util.Date;
import java.util.List;

public interface Task extends ApplicationElement {

    enum TaskStatus {
        CREATED,
        ASSIGNED,
        SUSPENDED,
        COMPLETED,
        CANCELLED,
        DELETED
    }

    String getId();

    String getOwner();

    String getAssignee();

    String getName();

    String getDescription();

    Date getCreatedDate();

    Date getClaimedDate();

    Date getDueDate();

    int getPriority();

    String getProcessDefinitionId();

    String getProcessInstanceId();

    String getParentTaskId();

    TaskStatus getStatus();

    String getFormKey();

    Date getCompletedDate();

    Long getDuration();
    
    Integer getProcessDefinitionVersion();
    
    String getBusinessKey();

    boolean isStandalone();
    
    String getTaskDefinitionKey();

    List<String> getCandidateUsers();

    List<String> getCandidateGroups();
}
