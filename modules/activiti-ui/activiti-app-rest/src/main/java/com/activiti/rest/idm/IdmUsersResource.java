/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
