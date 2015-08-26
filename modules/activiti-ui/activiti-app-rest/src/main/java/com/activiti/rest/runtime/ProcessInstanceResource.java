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

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.model.runtime.ProcessInstanceRepresentation;
import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for managing a process instance.
 */
@RestController
public class ProcessInstanceResource extends AbstractProcessInstanceResource {

    @RequestMapping(value = "/rest/process-instances/{processInstanceId}",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public ProcessInstanceRepresentation getProcessInstance(@PathVariable String processInstanceId, HttpServletResponse response) {
    	return super.getProcessInstance(processInstanceId, response);
    }
    
    @RequestMapping(value = "/rest/process-instances/{processInstanceId}/start-form",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public FormDefinitionRepresentation getProcessInstanceStartForm(@PathVariable String processInstanceId, HttpServletResponse response) {
    	return super.getProcessInstanceStartForm(processInstanceId, response);
    }
    
    @RequestMapping(value = "/rest/process-instances/{processInstanceId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @Timed
    public void deleteProcessInstance(@PathVariable String processInstanceId) {
    	super.deleteProcessInstance(processInstanceId);
    }

}
