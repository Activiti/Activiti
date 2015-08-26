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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.common.ResultListDataRepresentation;
import com.codahale.metrics.annotation.Timed;

/**
 * Rest resource for managing users, specifically related to tasks and processes.
 */
@RestController
public class WorkflowUsersResource extends AbstractWorkflowUsersResource {
	
    @Timed
    @RequestMapping(value = "/rest/workflow-users", method = RequestMethod.GET)
    public ResultListDataRepresentation getUsers(
    		@RequestParam(value="filter", required=false) String filter, 
    		@RequestParam(value="email", required=false) String email,
    		@RequestParam(value="externalId", required=false) String externalId,
            @RequestParam(value="excludeTaskId", required=false) String excludeTaskId,
            @RequestParam(value="excludeProcessId", required=false) String excludeProcessId,
            @RequestParam(value="groupId", required=false) Long groupId,
            @RequestParam(value="tenantId", required=false) Long tenantId) {
    	
    	return super.getUsers(filter, email, excludeTaskId, excludeProcessId, groupId);
    }
    
    @Timed
    @RequestMapping(value = "/rest/workflow-users/{userId}/recent-users", method = RequestMethod.GET)
    public ResultListDataRepresentation getRecentUsers(@PathVariable(value="userId") Long userId,
            @RequestParam(value="excludeTaskId", required=false) String excludeTaskId,
            @RequestParam(value="excludeProcessId", required=false) String excludeProcessId) {
        
        // TODO: Actually implement this using recent people instead of the full people list
    	
    	
    	// Disabled recent users for now
//        int page = 0;
//        int pageSize = MAX_RECENT_PEOPLE;
//        
//        List<User> matchingUsers = userService.findUsers("", false, UserStatus.ACTIVE, null, null, 
//        		SecurityUtils.getCurrentUserObject().getTenantId(), new PageRequest(page, pageSize));
//        
//        // Filter out users already part of the task/process of which the ID has been passed
//        if(excludeTaskId != null) {
//            filterUsersInvolvedInTask(excludeTaskId, matchingUsers);
//        } else if(excludeProcessId != null) {
//            filterUsersInvolvedInProcess(excludeProcessId, matchingUsers);
//        }
//        
//        List<LightUserRepresentation> resultList = new ArrayList<LightUserRepresentation>();
//        for(User user : matchingUsers) {
//            resultList.add(new LightUserRepresentation(user));
//        }
//        return new ResultListDataRepresentation(resultList);
    	return new ResultListDataRepresentation();
    }
    
}
