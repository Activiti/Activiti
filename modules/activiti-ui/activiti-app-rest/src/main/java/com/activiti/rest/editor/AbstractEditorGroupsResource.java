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
package com.activiti.rest.editor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.idm.LightGroupRepresentation;
import com.activiti.model.idm.LightUserRepresentation;
import com.activiti.service.api.GroupService;
import com.activiti.service.exception.NotFoundException;

public class AbstractEditorGroupsResource {
	
	private static final int MAX_GROUP_SIZE = 50;

    @Inject
    private GroupService groupService;
    
    public ResultListDataRepresentation getGroups(String filter) {
        
        int page = 0;
        int pageSize = MAX_GROUP_SIZE;
        
        List<Group> matchingGroups = groupService.getGroups(filter, page, pageSize); // true => only want to show the active groups in the pickers
        List<LightGroupRepresentation> resultList = new ArrayList<LightGroupRepresentation>();
        for (Group group : matchingGroups) {
            resultList.add(new LightGroupRepresentation(group));
        }
                
        ResultListDataRepresentation result = new ResultListDataRepresentation(resultList);
        // TODO: get total result count instead of page-count, in case the matching list's size is equal to the page size
        return result;
    }
    
    public ResultListDataRepresentation getUsersForGroup(Long groupId) {
    	
    	// Only works for functional groups
    	// (regular users shouldn't be allowed to get info from system groups)
    	Group group = groupService.getGroup(groupId);
    	
    	if (group == null) {
    		throw new NotFoundException();
    	}

    	List<LightUserRepresentation> groupUsers = new ArrayList<LightUserRepresentation>();
    	for (User user : group.getUsers()) {
    		groupUsers.add(new LightUserRepresentation(user));
    	}
    	return new ResultListDataRepresentation(groupUsers);
    }
    
}
