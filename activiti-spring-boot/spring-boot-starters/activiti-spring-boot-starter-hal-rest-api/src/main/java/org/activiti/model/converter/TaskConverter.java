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

import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Elias Ricken de Medeiros
 */
@Component
public class TaskConverter implements ModelConverter<Task, org.activiti.client.model.Task> {

    private final ListConverter listConverter;

    @Autowired
    public TaskConverter(ListConverter listConverter) {
        this.listConverter = listConverter;
    }

    @Override
    public org.activiti.client.model.Task from(Task task) {
        org.activiti.client.model.Task clientTask = new org.activiti.client.model.Task();
        clientTask.setId(task.getId());
        clientTask.setName(task.getName());
        clientTask.setAssignee(task.getAssignee());
        clientTask.setSuspended(task.isSuspended());
        clientTask.setCategory(task.getCategory());
        clientTask.setCreateTime(task.getCreateTime());
        clientTask.setDueDate(task.getDueDate());
        clientTask.setDescription(task.getDescription());
        clientTask.setOwner(task.getOwner());
        clientTask.setPriority(task.getPriority());
        clientTask.setTaskDefinitionKey(task.getTaskDefinitionKey());
        clientTask.setProcessDefinitionId(task.getProcessDefinitionId());
        clientTask.setProcessInstanceId(task.getProcessInstanceId());
        return clientTask;
    }

    @Override
    public List<org.activiti.client.model.Task> from(List<Task> tasks) {
        return listConverter.from(tasks, this);
    }

}
