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
package com.activiti.rest.idm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.idm.AbstractGroupRepresentation;
import com.activiti.model.idm.GroupRepresentation;
import com.activiti.model.idm.LightGroupRepresentation;
import com.activiti.model.idm.LightUserRepresentation;
import com.activiti.service.api.GroupService;
import com.activiti.service.api.UserService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.NotFoundException;

/**
 * @author Joram Barrez
 */
public class AbstractIdmGroupsResource {
    
    @Autowired
    private GroupService groupService;
    
    @Autowired
    private UserService userService;
    
    public List<LightGroupRepresentation> getGroups() {

		List<Group> groups = groupService.getGroups();

		List<LightGroupRepresentation> result = new ArrayList<LightGroupRepresentation>();

		for (Group group : groups) {
			result.add(new LightGroupRepresentation(group));
		}

		return result;
	}

    public AbstractGroupRepresentation getGroup(Long groupId, Boolean includeAllUsers, boolean summary) {
        
    	boolean includeUsersValue = includeAllUsers != null ? includeAllUsers.booleanValue() : true;
    	
    	Group group = groupService.getGroup(groupId, includeUsersValue);
    	
    	if (group == null) {
    		throw new NotFoundException();
    	}
    	
    	if (summary) {
    	    return new LightGroupRepresentation(group);
    	}
    	
    	GroupRepresentation groupRepresentation = new GroupRepresentation(group, includeUsersValue);
    	groupRepresentation.setUserCount(userService.countUsersForGroup(groupId, null));
    	return groupRepresentation;
    }
    
    public GroupRepresentation getGroup(Long groupId, Boolean includeAllUsers) {
        return (GroupRepresentation) getGroup(groupId, includeAllUsers, false);
    }
    
    public ResultListDataRepresentation getGroupUsers(Long groupId, String filter, Integer page, Integer pageSize) {
    	int pageValue = page != null ? page.intValue() : 0;
    	int pageSizeValue = pageSize != null ? pageSize.intValue() : 50;
    	List<User> users = userService.findUsersForGroup(groupId, filter, pageValue, pageSizeValue);
    	
    	List<LightUserRepresentation> dtos = new ArrayList<LightUserRepresentation>(users.size());
    	for (User user : users) {
    		dtos.add(new LightUserRepresentation(user));
    	}
    	
    	ResultListDataRepresentation resultListDataRepresentation = new ResultListDataRepresentation(dtos);
    	resultListDataRepresentation.setStart(pageValue * pageSizeValue);
    	resultListDataRepresentation.setSize(dtos.size());
    	resultListDataRepresentation.setTotal(userService.countUsersForGroup(groupId, filter).intValue());
    	return resultListDataRepresentation;
    }
    
    public GroupRepresentation createNewGroup(GroupRepresentation groupRepresentation) {
        if(StringUtils.isBlank(groupRepresentation.getName())) { 
            throw new BadRequestException("Group name required");
        }
    
    	Group newGroup = groupService.createGroup(groupRepresentation.getName());
    	return new GroupRepresentation(newGroup);
    }
    
    public GroupRepresentation updateGroup(Long groupId, GroupRepresentation groupRepresentation) {
    	
        if(StringUtils.isBlank(groupRepresentation.getName())) {
            throw new BadRequestException("Group name required");
        }
    
    	Group group = groupService.updateGroup(groupId, groupRepresentation.getName());
    	if (group == null) {
    		throw new NotFoundException();
    	}
    	return new GroupRepresentation(groupService.getGroup(groupId));
    }
    
    public void deleteGroup(Long groupId) {
    	
    	Group group = groupService.getGroup(groupId);
    	if (group == null) {
    		throw new NotFoundException();
    	}

    	groupService.deleteGroup(groupId);
    }

    public void addGroupMember(Long groupId, Long userId) {
    	verifySecurityForGroupMember(groupId, userId);
    	Group group = groupService.getGroup(groupId, false);
    	User user = userService.getUser(userId, false);
    	groupService.addUserToGroup(group, user);
    }
    
    public void deleteGroupMember(Long groupId, Long userId) {
    	verifySecurityForGroupMember(groupId, userId);
    	Group group = groupService.getGroup(groupId, false);
    	User user = userService.getUser(userId, false);
    	groupService.deleteUserFromGroup(group, user);
    }
    
 	private void verifySecurityForGroupMember(Long groupId, Long userId) {
	    // Check existence
    	Group group = groupService.getGroup(groupId);
    	User user = userService.getUser(userId);
    	for (User groupMember : group.getUsers()) {
    		if (groupMember.getId().equals(userId)) {
    			user = groupMember;
    		}
    	}
    	
    	if (group == null || user == null) {
    		throw new NotFoundException();
    	}
    }
    
    
}
