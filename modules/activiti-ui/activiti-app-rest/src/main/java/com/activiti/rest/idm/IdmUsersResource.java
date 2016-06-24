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

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.idm.BulkUserUpdateRepresentation;
import com.activiti.model.idm.UserOverviewRepresentation;
import com.activiti.model.idm.UserRepresentation;

/**
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@RestController
public class IdmUsersResource extends AbstractIdmUsersResource {
    
    /**
     * GET  /rest/admin/users/summary -> get an overview of the users in the system
     */
    @RequestMapping(value = "/rest/admin/users/summary", method = RequestMethod.GET)
    public UserOverviewRepresentation getOverview() {

        UserOverviewRepresentation result = new UserOverviewRepresentation();
        result.setTotalUserCount(userService.getUserCount());
        
        // Add counts by status
        // TODO: OTHER COUNT

//        long totalCount = 0;
//        Map<UserStatus, Number> userCountsByStatus = userService.getUserCountsByStatus();
//        for(Entry<UserStatus, Number> entry : userCountsByStatus.entrySet()) {
//            result.getStatusCount().put(entry.getKey().name(), entry.getValue().longValue());
//            totalCount += entry.getValue().longValue();
//        }
//
//        // Add counts by accountType
//        Map<AccountType, Number> userByAccountType = userService.getUserCountsByAccountType();
//        for(Entry<AccountType, Number> entry : userByAccountType.entrySet()) {
//            result.getAccountTypeCounts().put(entry.getKey().name(), entry.getValue().longValue());
//        }
        
//        result.setTotalUserCount(totalCount);
        return result;
    }
    
    @RequestMapping(value = "/rest/admin/users", method = RequestMethod.GET)
    public ResultListDataRepresentation getUsers(@RequestParam(required=false) String filter, 
            @RequestParam(required=false) String sort,
            @RequestParam(required=false) String company, 
            @RequestParam(required=false) Integer start,
            @RequestParam(required=false) Long groupId) {

    	return super.getUsers(filter, sort, company, start, null, null, groupId);
    }

    @RequestMapping(value = "/rest/admin/users/{userId}", method = RequestMethod.PUT)
    public void updateUserDetails(@PathVariable Long userId, @RequestBody UserRepresentation userRepresentation) {
    	super.updateUserDetails(userId, userRepresentation);
    }
    
    @RequestMapping(value = "/rest/admin/users", method = RequestMethod.PUT)
    public void bulkUpdateUsers(@RequestBody BulkUserUpdateRepresentation update) {
    	super.bulkUpdateUsers(update);
    }
    
    @RequestMapping(value = "/rest/admin/users", method = RequestMethod.POST)
    public UserRepresentation createNewUser(@RequestBody UserRepresentation userRepresentation) {
    	return super.createNewUser(userRepresentation);
    }
    
}
