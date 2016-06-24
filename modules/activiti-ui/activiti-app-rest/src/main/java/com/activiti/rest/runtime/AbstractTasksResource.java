/* Licensed under the Apache License, Version 2.0 (the "License");
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
 */
package com.activiti.rest.runtime;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;

import com.activiti.model.runtime.TaskRepresentation;
import com.activiti.service.exception.BadRequestException;

/**
 * REST controller for managing the current user's account.
 */
public class AbstractTasksResource {

    @Inject
    protected TaskService taskService;
    
    public TaskRepresentation createNewTask(TaskRepresentation taskRepresentation, HttpServletRequest request) {
        if (StringUtils.isEmpty(taskRepresentation.getName())) {
            throw new BadRequestException("Task name is required");
        }
        
        if (StringUtils.isNotEmpty(taskRepresentation.getId())) {
            throw new BadRequestException("Task id should be empty");
        }
        
        Task task = taskService.newTask();
        
        taskRepresentation.fillTask(task);
        
        if (taskRepresentation.getAssignee() != null && taskRepresentation.getAssignee().getId() != null) {
            task.setAssignee(String.valueOf(taskRepresentation.getAssignee().getId()));
        }
        
        taskService.saveTask(task);
        
        return new TaskRepresentation(taskService.createTaskQuery().taskId(task.getId()).singleResult());
    }
}
