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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.idm.User;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.idm.LightUserRepresentation;
import com.activiti.model.runtime.TaskRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.UserCache;
import com.activiti.service.api.UserCache.CachedUser;
import com.activiti.service.api.UserService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.NotPermittedException;
import com.activiti.service.runtime.PermissionService;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class HistoricTaskQueryResource {

    @Inject
    protected HistoryService historyService;
    
    @Inject
    protected UserService userService;
    
    @Inject
    protected UserCache userCache;
    
    @Inject
    protected PermissionService permissionService;
    
	@RequestMapping(value = "/rest/query/history/tasks", method = RequestMethod.POST, produces = "application/json")
	@Timed
	public ResultListDataRepresentation listTasks(@RequestBody ObjectNode requestNode) {
	    if (requestNode == null) {
	        throw new BadRequestException("No request found");
	    }
	    
	    HistoricTaskInstanceQuery taskQuery = historyService.createHistoricTaskInstanceQuery();
	    
	    User currentUser = SecurityUtils.getCurrentUserObject();

	    JsonNode processInstanceIdNode = requestNode.get("processInstanceId");
	    if (processInstanceIdNode != null && processInstanceIdNode.isNull() == false) {
            String processInstanceId = processInstanceIdNode.asText();
            if (permissionService.hasReadPermissionOnProcessInstance(currentUser, processInstanceId)) {
            	taskQuery.processInstanceId(processInstanceId);
            } else {
            	throw new NotPermittedException();
            }
	    }
	    
	    JsonNode finishedNode = requestNode.get("finished");
	    if (finishedNode != null && finishedNode.isNull() == false) {
	        boolean isFinished = finishedNode.asBoolean();
	        if (isFinished) {
	            taskQuery.finished();
	        } else {
	            taskQuery.unfinished();
	        }
	    }
	    
	    List<HistoricTaskInstance> tasks = taskQuery.list();
	    
	    // get all users to have the user object available in the task on the client side
	    ResultListDataRepresentation result = new ResultListDataRepresentation(convertTaskInfoList(tasks));
	    return result;
	}
	
	protected List<TaskRepresentation> convertTaskInfoList(List<HistoricTaskInstance> tasks) {
	    List<TaskRepresentation> result = new ArrayList<TaskRepresentation>();
	    if (CollectionUtils.isNotEmpty(tasks)) {
	        TaskRepresentation representation = null;
	        for (HistoricTaskInstance task : tasks) {
	            representation = new TaskRepresentation(task);
	            
	            CachedUser cachedUser = userCache.getUser(task.getAssignee());
	        	if (cachedUser != null && cachedUser.getUser() != null) {
	                representation.setAssignee(new LightUserRepresentation(cachedUser.getUser()));
	        	}
	            
                result.add(representation);
            }
	    }
	    return result;
	}
}
