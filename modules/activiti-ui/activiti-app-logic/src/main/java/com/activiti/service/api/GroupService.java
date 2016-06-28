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
package com.activiti.service.api;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;

import java.util.List;

/**
 * @author jbarrez
 */
public interface GroupService {
	
	// Get
	
	Group getGroup(Long groupId);
	    
	Group getGroup(Long groupId, boolean initUsers);

    List<Group> getGroups(String filter, int skip, int maxResults);

    List<Group> getGroups();

    // Create
    
    Group createGroup(String name);

    // Update

    Group updateGroup(Long groupId, String name);
    
    Group save(Group group);
    
    // Delete

    void deleteGroup(Long groupId);
    
    // User managent

    boolean addUserToGroup(Group group, User user);

    void deleteUserFromGroup(Group group, User user);
}
    
