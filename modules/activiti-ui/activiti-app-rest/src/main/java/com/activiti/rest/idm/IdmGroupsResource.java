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

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.idm.GroupRepresentation;
import com.activiti.model.idm.LightGroupRepresentation;

/**
 * @author Joram Barrez
 */
@RestController
@RequestMapping(value="/rest/admin/groups")
public class IdmGroupsResource extends AbstractIdmGroupsResource {
    
    @RequestMapping(method = RequestMethod.GET)
    public List<LightGroupRepresentation> getGroups() {
    	return super.getGroups();
    }

    @RequestMapping(value="/{groupId}", method = RequestMethod.GET)
    public GroupRepresentation getGroup(@PathVariable Long groupId, @RequestParam(required=false) Boolean includeAllUsers) {
        return super.getGroup(groupId, includeAllUsers);
    }
    
    @RequestMapping(value="/{groupId}/users", method = RequestMethod.GET)
    public ResultListDataRepresentation  getGroupUsers(@PathVariable Long groupId, @RequestParam(required=false) String filter,
    		@RequestParam(required=false) Integer page, @RequestParam(required=false) Integer pageSize) {
    	return super.getGroupUsers(groupId, filter, page, pageSize);
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public GroupRepresentation createNewGroup(@RequestBody GroupRepresentation groupRepresentation) {
    	return super.createNewGroup(groupRepresentation);
    }
    
    @RequestMapping(value="/{groupId}", method = RequestMethod.PUT)
    public GroupRepresentation updateGroup(@PathVariable Long groupId, @RequestBody GroupRepresentation groupRepresentation) {
    	return super.updateGroup(groupId, groupRepresentation);
    }
    
    @RequestMapping(value="/{groupId}", method = RequestMethod.DELETE)
    public void deleteGroup(@PathVariable Long groupId) {
    	super.deleteGroup(groupId);
    }
    
    @RequestMapping(value="/{groupId}/members/{userId}", method = RequestMethod.POST)
    public void addGroupMember(@PathVariable Long groupId, @PathVariable Long userId) {
    	super.addGroupMember(groupId, userId);
    }
    

    @RequestMapping(value="/{groupId}/members/{userId}", method = RequestMethod.DELETE)
    public void deleteGroupMember(@PathVariable Long groupId, @PathVariable Long userId) {
    	super.deleteGroupMember(groupId, userId);
    }
}
