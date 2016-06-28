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
