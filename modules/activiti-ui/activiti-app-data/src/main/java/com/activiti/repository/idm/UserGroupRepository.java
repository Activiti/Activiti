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
package com.activiti.repository.idm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.activiti.domain.idm.UserGroup;
import com.activiti.domain.idm.UserGroup.UserGroupPK;

public interface UserGroupRepository extends JpaRepository<UserGroup, UserGroupPK> {
	
	 @Query("select count(ug.userGroupPK.userId) from UserGroup ug where ug.userGroupPK.userId = :userId and ug.userGroupPK.groupId = :groupId")
	 Long getCount(@Param("userId") Long userId, @Param("groupId") Long groupId);
	 
	 @Modifying
	 @Query(value="delete from UserGroup ug where ug.userGroupPK.userId = :userId and ug.userGroupPK.groupId = :groupId")
	 void deleteUserGroup(@Param("userId") Long userId, @Param("groupId") Long groupId);
	    

}
