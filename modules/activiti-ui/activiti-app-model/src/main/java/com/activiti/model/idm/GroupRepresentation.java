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
package com.activiti.model.idm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;

/**
 * @author Joram Barrez
 */
public class GroupRepresentation extends AbstractGroupRepresentation {
	
	protected Long userCount;
	protected List<UserRepresentation> users;

	public GroupRepresentation() {
		
	}
	
	public GroupRepresentation(Group group) {
		this(group, true);
	}
	
	public GroupRepresentation(Group group, boolean includeUsers) {
	    super(group);

		// Users
		if (includeUsers) {
			if (group.getUsers() != null) {
				this.users = new ArrayList<UserRepresentation>(group.getUsers().size());
				
				for (User user : group.getUsers()) {
					this.users.add(new UserRepresentation(user));
				}
				
				// Sort by last name
				Collections.sort(this.users, new Comparator<UserRepresentation>() {

					@Override
	                public int compare(UserRepresentation user1, UserRepresentation user2) {
						if (user1.getLastName() != null && user2.getLastName() != null) {
							return user1.getLastName().compareTo(user2.getLastName());
						} else if (user1.getLastName() == null) {
							return -1;
						} else {
							return 1;
						}
	   	             }
				});
			}
		}
	}

	public Long getUserCount() {
		return userCount;
	}
	public void setUserCount(Long userCount) {
		this.userCount = userCount;
	}
	public List<UserRepresentation> getUsers() {
		return users;
	}
	public void setUsers(List<UserRepresentation> users) {
		this.users = users;
	}
}
