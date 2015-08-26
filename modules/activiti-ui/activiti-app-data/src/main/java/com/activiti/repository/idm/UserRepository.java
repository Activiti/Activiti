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

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.activiti.domain.idm.User;

/**
 * Spring Data JPA repository for the User entity.
 */
public interface UserRepository extends JpaRepository<User, Long> {
	
	//
	// SELECTS
	//

    User findByEmail(String email);
    
    @Query("select u from User u left join fetch u.groups where u.email = :email")
    User findByEmailFetchGroups(@Param("email") String email);

    @Query("select count(u.id) from User u")
    Long getUserCount();
    
    @Query("select count(u.id) from User u where u.id = :userId and lastUpdate = :lastUpdate")
    Long getUserCountByUserIdAndLastUpdateDate(@Param("userId") Long userId, @Param("lastUpdate") Date lastUpdate);
    
    @Query("select distinct share.user from ModelShareInfo share where share.sharedBy.id = :userId")
    List<User> findUsersSharedWithRecently(@Param("userId") Long userId, Pageable paging);
    
    @Query("select distinct share.user from ModelShareInfo share where share.sharedBy.id = :userId and share.model.id != :excludeModelId")
    List<User> findUsersSharedWithRecentlyExcludeModel(@Param("userId") Long userId, @Param("excludeModelId") Long excludeModelId, Pageable paging);
    

    @Query("select u from Group g inner join g.users u where g.id = :groupId")
    List<User> findUsersForGroup(@Param("groupId") Long groupId, Pageable pageable);
    
    @Query("select u.id from Group g inner join g.users u where g.id = :groupId")
    List<Long> findUserIdsForGroup(@Param("groupId") Long groupId, Pageable pageable);
    
    @Query("select u.email from Group g inner join g.users u where g.id = :groupId")
    List<String> findUserEmailsForGroup(@Param("groupId") Long groupId, Pageable pageable);
    
    @Query("select count(u.id) from Group g inner join g.users u where g.id = :groupId")
    Long countUsersForGroup(@Param("groupId") Long groupId);

    @Query("select u from Group g inner join g.users u where g.id = :groupId and (lower(u.firstName) like %:filter% or lower(u.lastName) like %:filter% or lower(u.email) like %:filter%)")
    List<User> findUsersForGroup(@Param("groupId") Long groupId, @Param("filter") String filter, Pageable pageable);
    
    @Query("select count(u.id) from Group g inner join g.users u where g.id = :groupId and (lower(u.firstName) like %:filter% or lower(u.lastName) like %:filter% or lower(u.email) like %:filter%)")
    Long countUsersForGroup(@Param("groupId") Long groupId, @Param("filter") String filter);

    // 
    // UPDATES
    //
    
    @Modifying
	@Query(value="update User u set u.lastUpdate = :lastUpdate where u.id = :id")
	void changeLastUpdateValue(@Param("id") Long id, @Param("lastUpdate") Date lastUpdate);
    
}
