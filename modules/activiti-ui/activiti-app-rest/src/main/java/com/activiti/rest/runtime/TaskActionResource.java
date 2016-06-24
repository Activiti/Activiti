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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.runtime.TaskRepresentation;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class TaskActionResource extends AbstractTaskActionResource {
    
	@RequestMapping(value = "/rest/tasks/{taskId}/action/complete", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.OK)
    public void completeTask(@PathVariable String taskId) {
		super.completeTask(taskId);
    }
	
	@RequestMapping(value = "/rest/tasks/{taskId}/action/assign", method = RequestMethod.PUT)
    public TaskRepresentation assignTask(@PathVariable String taskId, @RequestBody ObjectNode requestNode) {
		return super.assignTask(taskId, requestNode);
    }
	
	@RequestMapping(value = "/rest/tasks/{taskId}/action/involve", method = RequestMethod.PUT, produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	public void involveUser(@PathVariable("taskId") String taskId, @RequestBody ObjectNode requestNode) {
		super.involveUser(taskId, requestNode);
	}
	
	@RequestMapping(value = "/rest/tasks/{taskId}/action/remove-involved", method = RequestMethod.PUT, produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	public void removeInvolvedUser(@PathVariable("taskId") String taskId, @RequestBody ObjectNode requestNode) {
		super.removeInvolvedUser(taskId, requestNode);
	}
	
	@RequestMapping(value = "/rest/tasks/{taskId}/action/claim", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.OK)
    public void claimTask(@PathVariable String taskId) {
		super.claimTask(taskId);
    }

}
