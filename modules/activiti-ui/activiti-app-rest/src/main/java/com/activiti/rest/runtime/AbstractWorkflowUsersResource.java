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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.springframework.data.domain.PageRequest;

import com.activiti.domain.idm.User;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.idm.LightUserRepresentation;
import com.activiti.service.api.UserService;

public class AbstractWorkflowUsersResource {
	
	private static final int MAX_PEOPLE_SIZE = 50;

    @Inject
    private UserService userService;
    
    @Inject 
    private RuntimeService runtimeService;
    
    @Inject
    private TaskService taskService;
    
    public ResultListDataRepresentation getUsers(String filter, String email, String excludeTaskId, String excludeProcessId, Long groupId) {
    	
    	// Actual query
    	int page = 0;
    	int pageSize = MAX_PEOPLE_SIZE;
    	
        List<User> matchingUsers = userService.findUsers(filter, false, email, null, groupId, new PageRequest(page, pageSize));
        
        // Filter out users already part of the task/process of which the ID has been passed
        if (excludeTaskId != null) {
            filterUsersInvolvedInTask(excludeTaskId, matchingUsers);
        } else if(excludeProcessId != null) {
            filterUsersInvolvedInProcess(excludeProcessId, matchingUsers);
        }
        
        List<LightUserRepresentation> resultList = new ArrayList<LightUserRepresentation>();
        for(User user : matchingUsers) {
            resultList.add(new LightUserRepresentation(user));
        }
                
        ResultListDataRepresentation result = new ResultListDataRepresentation(resultList);
        
        if(page!=0 || (page == 0 && matchingUsers.size() == pageSize)) {
            // Total differs from actual result size, need to fetch it
            result.setTotal(userService.countUsers(filter, false, email, null, null).intValue());
        }
        return result ;
    }
    
    protected void filterUsersInvolvedInProcess(String excludeProcessId, List<User> matchingUsers) {
        Set<String> involvedUsers = getInvolvedUsersAsSet(runtimeService.getIdentityLinksForProcessInstance(excludeProcessId));
        removeinvolvedUsers(matchingUsers, involvedUsers);
    }
    
    protected void filterUsersInvolvedInTask(String excludeTaskId, List<User> matchingUsers) {
        Set<String> involvedUsers = getInvolvedUsersAsSet(taskService.getIdentityLinksForTask(excludeTaskId));
        removeinvolvedUsers(matchingUsers, involvedUsers);
    }
    
    protected Set<String> getInvolvedUsersAsSet(List<IdentityLink> involvedPeople) {
        Set<String> involved = null;
        if(involvedPeople.size() > 0) {
            involved = new HashSet<String>();
            for(IdentityLink link : involvedPeople) {
                if(link.getUserId() != null) {
                    involved.add(link.getUserId());
                }
            }
        }
        return involved;
    }
    
    protected void removeinvolvedUsers(List<User> matchingUsers, Set<String> involvedUsers) {
        if(involvedUsers != null) {
            // Using iterator to be able to remove without ConcurrentModExceptions
            Iterator<User> userIt = matchingUsers.iterator();
            while(userIt.hasNext()) {
                if(involvedUsers.contains(userIt.next().getId().toString())) {
                    userIt.remove();
                }
            }
        }
    }
    
}
