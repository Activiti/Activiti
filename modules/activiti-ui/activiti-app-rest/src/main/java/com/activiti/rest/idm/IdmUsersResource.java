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

import org.activiti.engine.identity.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.common.ResultListDataRepresentation;

/**
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@RestController
public class IdmUsersResource extends AbstractIdmUsersResource {
    
    @RequestMapping(value = "/rest/admin/users", method = RequestMethod.GET)
    public ResultListDataRepresentation getUsers(@RequestParam(required=false) String filter, 
            @RequestParam(required=false) String sort,
            @RequestParam(required=false) Integer start,
            @RequestParam(required=false) String groupId) {

    	return super.getUsers(filter, sort, start, null, groupId);
    }

    @RequestMapping(value = "/rest/admin/users/{userId}", method = RequestMethod.PUT)
    public void updateUserDetails(@PathVariable String userId, @RequestBody User userRepresentation) {
    	//super.updateUserDetails(userId, userRepresentation);
    }
 
    
    @RequestMapping(value = "/rest/admin/users", method = RequestMethod.POST)
    public User createNewUser(@RequestBody User userRepresentation) {
    	//return super.createNewUser(userRepresentation);
      return userRepresentation;
    }
    
}
