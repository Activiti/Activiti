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