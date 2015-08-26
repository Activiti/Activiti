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
