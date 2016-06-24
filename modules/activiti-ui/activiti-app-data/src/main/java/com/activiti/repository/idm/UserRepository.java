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
