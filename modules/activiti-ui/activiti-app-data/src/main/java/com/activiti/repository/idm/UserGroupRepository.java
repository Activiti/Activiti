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
