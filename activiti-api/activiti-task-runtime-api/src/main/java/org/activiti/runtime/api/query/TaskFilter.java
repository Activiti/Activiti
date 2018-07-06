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

package org.activiti.runtime.api.query;

import java.util.List;

public class TaskFilter {

    private String assigneeId;
    private List<String> groups;
    private String processInstanceId;


    //Todo: review this concept of assignee or candidate. Usually when we have two fields for filtering
    //the AND operator is used, here it will be OR. It might be confusing... probably we need an object
    //for grouping both in the same criteria
    public static TaskFilter filteredOnAssigneeOrCandiate(String assigneeId, List<String> groups){
        TaskFilter taskFilter = new TaskFilter();
        taskFilter.setAssigneeId(assigneeId);
        taskFilter.setGroups(groups);
        return taskFilter;
    }

    public static TaskFilter unfiltered(){
        return new TaskFilter();
    }

    public static TaskFilter filteredOnProcessInstanceId(String processInstanceId) {
        TaskFilter taskFilter = new TaskFilter();
        taskFilter.setProcessInstanceId(processInstanceId);
        return taskFilter;
    }


    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
