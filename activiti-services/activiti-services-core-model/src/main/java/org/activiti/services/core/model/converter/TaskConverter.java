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

package org.activiti.services.core.model.converter;

import java.util.List;

import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskConverter implements ModelConverter<Task, org.activiti.services.core.model.Task> {

    private final ListConverter listConverter;

    @Autowired
    public TaskConverter(ListConverter listConverter) {
        this.listConverter = listConverter;
    }

    @Override
    public org.activiti.services.core.model.Task from(Task source) {
        org.activiti.services.core.model.Task task = null;
        if (source != null) {
            task = new org.activiti.services.core.model.Task(source.getId(),
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
                                                                           source.getParentTaskId(),
                                                                           calculateStatus(source));
        }
        return task;
    }

    private String calculateStatus(Task source) {
        if (source.isSuspended()) {
            return org.activiti.services.core.model.Task.TaskStatus.SUSPENDED.name();
        } else if (source.getAssignee() != null && !source.getAssignee().isEmpty()) {
            return org.activiti.services.core.model.Task.TaskStatus.ASSIGNED.name();
        }
        return org.activiti.services.core.model.Task.TaskStatus.CREATED.name();
    }

    @Override
    public List<org.activiti.services.core.model.Task> from(List<Task> tasks) {
        return listConverter.from(tasks,
                                  this);
    }
}
