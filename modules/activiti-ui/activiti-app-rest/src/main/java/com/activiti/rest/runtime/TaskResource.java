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
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.runtime.TaskRepresentation;
import com.activiti.model.runtime.TaskUpdateRepresentation;
import com.activiti.service.runtime.PermissionService;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class TaskResource extends AbstractTaskResource {

    private final Logger log = LoggerFactory.getLogger(TaskResource.class);
    
    @Inject
    protected TaskService taskService;
    
    @Inject
    protected PermissionService permissionService;
    
    @Inject
    protected RepositoryService repositoryService;
    
    @RequestMapping(value = "/rest/tasks/{taskId}",
            method = RequestMethod.GET,
            produces = "application/json")
    public TaskRepresentation getTask(@PathVariable String taskId, HttpServletResponse response) {
    	return super.getTask(taskId, response);
    }

    @RequestMapping(value = "/rest/tasks/{taskId}",
            method = RequestMethod.PUT,
            produces = "application/json")
    public TaskRepresentation updateTask(@PathVariable("taskId") String taskId, @RequestBody TaskUpdateRepresentation updated) {
        return super.updateTask(taskId, updated);
    }
    
}
