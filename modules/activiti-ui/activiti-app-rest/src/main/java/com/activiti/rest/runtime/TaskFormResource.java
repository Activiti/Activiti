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

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.model.runtime.CompleteFormRepresentation;
import com.activiti.model.runtime.ProcessInstanceVariableRepresentation;
import com.codahale.metrics.annotation.Timed;

/**
 * @author Joram Barrez
 */
@RestController
@RequestMapping("/rest/task-forms")
public class TaskFormResource extends AbstractTaskFormResource {
    
	@Timed
	@RequestMapping(value="/{taskId}", method = RequestMethod.GET, produces = "application/json")
	public FormDefinitionRepresentation getTaskForm(@PathVariable String taskId) {
	    return super.getTaskForm(taskId);
	}
	
	@Timed
	@RequestMapping(value="/{taskId}", method = RequestMethod.POST, produces = "application/json")
	public void completeTaskForm(@PathVariable String taskId, @RequestBody CompleteFormRepresentation completeTaskFormRepresentation) {
		super.completeTaskForm(taskId, completeTaskFormRepresentation);
	}
	
    @Timed
    @RequestMapping(value="/{taskId}/variables", method = RequestMethod.GET, produces = "application/json")
    public List<ProcessInstanceVariableRepresentation> getProcessInstanceVariables(@PathVariable String taskId) {
        return super.getProcessInstanceVariables(taskId);
    }
}
