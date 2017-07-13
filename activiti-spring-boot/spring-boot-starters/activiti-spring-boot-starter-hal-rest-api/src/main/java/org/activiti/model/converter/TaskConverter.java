/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.model.converter;

import java.util.List;

import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**

 */
@Component
public class TaskConverter implements ModelConverter<Task, org.activiti.client.model.Task> {

    private final ListConverter listConverter;

    @Autowired
    public TaskConverter(ListConverter listConverter) {
        this.listConverter = listConverter;
    }

    @Override
    public org.activiti.client.model.Task from(Task source) {
        org.activiti.client.model.Task task = null;
        if (source != null) {
            task = new org.activiti.client.model.Task(source.getId(),
                                                      source.getOwner(),
                                                      source.getAssignee(),
                                                      source.getName(),
                                                      source.getDescription(),
                                                      source.getCreateTime(),
                                                      source.getClaimTime(),
                                                      source.getDueDate(),
                                                      source.getPriority(),
                                                      source.getProcessDefinitionId(),
                                                      source.getProcessInstanceId(),
                                                      calculateStatus(source));
        }
        return task;
    }

    private String calculateStatus(Task source) {
        if(source.isSuspended()){
            return org.activiti.client.model.Task.TaskStatus.SUSPENDED.name();
        }else if(source.getAssignee() != null && !source.getAssignee().isEmpty()) {
            return org.activiti.client.model.Task.TaskStatus.ASSIGNED.name();
        }
        return org.activiti.client.model.Task.TaskStatus.CREATED.name();
    }

    @Override
    public List<org.activiti.client.model.Task> from(List<Task> tasks) {
        return listConverter.from(tasks,
                                  this);
    }
}
