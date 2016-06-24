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

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.activiti.domain.common.ImageUpload;
import com.activiti.domain.idm.User;

public interface UserService {

	User getUser(Long userId);

	User getUser(Long userId, boolean initGroups);
	
	List<User> getAllUsers(int page, int size);

	Long getUserCount();

	/**
	 * Creates a new user with the given details. Depending on the initial
	 * status, additional actions will be performed (eg. send activation email).
	 * 
	 * @throws IllegalArgumentException
	 *             when a required argument is null
	 * @throws IllegalStateException
	 *             when a user with the given email-address is already
	 *             registered
	 */
	User createNewUser(String email, String firstName, String lastName, String password, String company);
	
	User createNewUserHashedPassword(String email, String firstName, String lastName, String password, String company);

	/**
	 * @return true, if old password matches and the password has been changed
	 *         to the new one. Returns false, if the old password doesn't match,
	 *         the password is NOT updated.
	 */
	boolean changePassword(Long userId, String oldPassword, String newPassword);

	User findUser(long userId);

	User findUserByEmail(String email);
	
	User findUserByEmailFetchGroups(String email);

	/**
	 * Looks up a {@link User} by the email address.
	 * When no user is found, a new user is created, with the status UserStatus.Pending.
	 */
	User findOrCreateUserByEmail(String email);

	List<User> findUsersForGroup(Long groupId, String filter, int page, int pageSize);
	
	Long countUsersForGroup(Long groupId, String filter);
	
	void changePassword(Long userId, String newPassword);

	List<User> findUsers(String filter, boolean applyFilterOnEmail, String email, String company, Long groupId, Pageable pageable);

	Long countUsers(String filter, boolean applyFilterOnEmail, String email, String company, Long groupId);
	
	List<User> getRecentUsersExcludeModel(Long userId, Long modelId);

	User updateUser(Long userId, String email, String firstName, String lastName, String company);

	ImageUpload updateUserPicture(MultipartFile file, Long userId) throws IOException;

	Long getUserCountByUserIdAndLastUpdateDate(Long userId, Date lastUpdate);
	
	User save(User user);
	
}