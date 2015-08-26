/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
