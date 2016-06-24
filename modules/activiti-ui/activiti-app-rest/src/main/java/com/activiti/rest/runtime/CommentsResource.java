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

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.runtime.CommentRepresentation;

/**
 * REST resource related to comment collection on tasks and process instances.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@RestController
public class CommentsResource extends AbstractCommentsResource {

    @RequestMapping(value = "/rest/tasks/{taskId}/comments", method = RequestMethod.GET, produces = "application/json")
    public ResultListDataRepresentation getTaskComments(@PathVariable("taskId") String taskId,
            @RequestParam(value = "latestFirst", required = false) Boolean latestFirst) {

    	return super.getTaskComments(taskId, latestFirst);
    	
    }

    @RequestMapping(value = "/rest/tasks/{taskId}/comments", method = RequestMethod.POST, produces = "application/json")
    public CommentRepresentation addTaskComment(@RequestBody CommentRepresentation commentRequest,
            @PathVariable("taskId") String taskId) {

        return super.addTaskComment(commentRequest, taskId);
    	
    }
    
    @RequestMapping(value = "/rest/process-instances/{processInstanceId}/comments", method = RequestMethod.GET, produces = "application/json")
    public ResultListDataRepresentation getProcessInstanceComments(@PathVariable("processInstanceId") String processInstanceId,
            @RequestParam(value = "latestFirst", required = false) Boolean latestFirst) {

    	return super.getProcessInstanceComments(processInstanceId, latestFirst);
       
    }
    
    @RequestMapping(value = "/rest/process-instances/{processInstanceId}/comments", method = RequestMethod.POST, produces = "application/json")
    public CommentRepresentation addProcessInstanceComment(@RequestBody CommentRepresentation commentRequest,
            @PathVariable("processInstanceId") String processInstanceId) {

        return super.addProcessInstanceComment(commentRequest, processInstanceId);
    	
    }

}
