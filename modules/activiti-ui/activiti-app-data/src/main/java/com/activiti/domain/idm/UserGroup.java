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
package com.activiti.domain.idm;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "USER_GROUP")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserGroup implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
    private UserGroupPK userGroupPK;
	
	
	public UserGroupPK getUserGroupPK() {
		return userGroupPK;
	}

	public void setUserGroupPK(UserGroupPK userGroupPK) {
		this.userGroupPK = userGroupPK;
	}

	@Embeddable
	public static class UserGroupPK implements Serializable {
		
		@Column(name="user_id")
		private Long userId;
		
		@Column(name="group_id")
		private Long groupId;
		
		public UserGroupPK() {
			
		}
		
		public UserGroupPK(Long userId, Long groupId) {
			this.userId = userId;
			this.groupId = groupId;
		}

		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		public Long getGroupId() {
			return groupId;
		}

		public void setGroupId(Long groupId) {
			this.groupId = groupId;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof UserGroupPK)) {
				return false;
			}
			
			UserGroupPK other = (UserGroupPK) obj;
			return other.getGroupId().equals(groupId) && other.getUserId().equals(groupId);
		}
		
		@Override
		public int hashCode() {
			return (groupId + userId + "").hashCode();
		}
		
	}
	
	
}