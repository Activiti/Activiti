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

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.idm.AddGroupCapabilitiesRepresentation;
import com.activiti.model.idm.GroupRepresentation;
import com.activiti.model.idm.LightGroupRepresentation;
import com.codahale.metrics.annotation.Timed;

/**
 * @author Joram Barrez
 */
@RestController
@RequestMapping(value="/rest/admin/groups")
public class IdmGroupsResource extends AbstractIdmGroupsResource {
    
    @Timed
    @RequestMapping(method = RequestMethod.GET)
    public List<LightGroupRepresentation> getGroups() {
    	return super.getGroups();
    }

    @Timed
    @RequestMapping(value="/{groupId}", method = RequestMethod.GET)
    public GroupRepresentation getGroup(@PathVariable Long groupId, @RequestParam(required=false) Boolean includeAllUsers) {
        return super.getGroup(groupId, includeAllUsers);
    }
    
    @Timed
    @RequestMapping(value="/{groupId}/users", method = RequestMethod.GET)
    public ResultListDataRepresentation  getGroupUsers(@PathVariable Long groupId, @RequestParam(required=false) String filter,
    		@RequestParam(required=false) Integer page, @RequestParam(required=false) Integer pageSize) {
    	return super.getGroupUsers(groupId, filter, page, pageSize);
    }
    
    @Timed
    @RequestMapping(method = RequestMethod.POST)
    public GroupRepresentation createNewGroup(@RequestBody GroupRepresentation groupRepresentation) {
    	return super.createNewGroup(groupRepresentation);
    }
    
    @Timed
    @RequestMapping(value="/{groupId}", method = RequestMethod.PUT)
    public GroupRepresentation updateGroup(@PathVariable Long groupId, @RequestBody GroupRepresentation groupRepresentation) {
    	return super.updateGroup(groupId, groupRepresentation);
    }
    
    @Timed
    @RequestMapping(value="/{groupId}", method = RequestMethod.DELETE)
    public void deleteGroup(@PathVariable Long groupId) {
    	super.deleteGroup(groupId);
    }
    
    @Timed
    @RequestMapping(value="/{groupId}/members/{userId}", method = RequestMethod.POST)
    public void addGroupMember(@PathVariable Long groupId, @PathVariable Long userId) {
    	super.addGroupMember(groupId, userId);
    }
    
    @Timed
    @RequestMapping(value="/{groupId}/members/{userId}", method = RequestMethod.DELETE)
    public void deleteGroupMember(@PathVariable Long groupId, @PathVariable Long userId) {
    	super.deleteGroupMember(groupId, userId);
    }
}
