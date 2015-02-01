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
package org.activiti.engine;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.NativeGroupQuery;
import org.activiti.engine.identity.NativeUserQuery;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;

import java.util.List;


/**
 * Service to manage {@link User}s and {@link Group}s.
 * 
 * @author Tom Baeyens
 */
public interface IdentityService {

  /**
   * Creates a new user. The user is transient and must be saved using 
   * {@link #saveUser(User)}.
   * @param userId id for the new user, cannot be null.
   */
  User newUser(String userId);
  
  /**
   * Saves the user. If the user already existed, the user is updated.
   * @param user user to save, cannot be null.
   * @throws RuntimeException when a user with the same name already exists.
   */
  void saveUser(User user);
  
  /**
   * Creates a {@link UserQuery} that allows to programmatically query the users.
   */
  UserQuery createUserQuery();

  /**
   * Returns a new {@link org.activiti.engine.query.NativeQuery} for tasks.
   */
  NativeUserQuery createNativeUserQuery();
  
  /**
   * @param userId id of user to delete, cannot be null. When an id is passed
   * for an unexisting user, this operation is ignored.
   */
  void deleteUser(String userId);
  
  /**
   * Creates a new group. The group is transient and must be saved using 
   * {@link #saveGroup(Group)}.
   * @param groupId id for the new group, cannot be null.
   */
  Group newGroup(String groupId);
  
  /**
   * Creates a {@link GroupQuery} thats allows to programmatically query the groups.
   */
  GroupQuery createGroupQuery();

  /**
   * Returns a new {@link org.activiti.engine.query.NativeQuery} for tasks.
   */
  NativeGroupQuery createNativeGroupQuery();
  
  /**
   * Saves the group. If the group already existed, the group is updated.
   * @param group group to save. Cannot be null.
   * @throws RuntimeException when a group with the same name already exists.
   */
  void saveGroup(Group group);
  
  /**
   * Deletes the group. When no group exists with the given id, this operation
   * is ignored.
   * @param groupId id of the group that should be deleted, cannot be null.
   */
  void deleteGroup(String groupId);

  /**
   * @param userId the userId, cannot be null.
   * @param groupId the groupId, cannot be null.
   * @throws RuntimeException when the given user or group doesn't exist or when the user
   * is already member of the group.
   */
  void createMembership(String userId, String groupId);
  
  /**
   * Delete the membership of the user in the group. When the group or user don't exist 
   * or when the user is not a member of the group, this operation is ignored.
   * @param userId the user's id, cannot be null.
   * @param groupId the group's id, cannot be null.
   */
  void deleteMembership(String userId, String groupId);

  /**
   * Checks if the password is valid for the given user. Arguments userId
   * and password are nullsafe.
   */
  boolean checkPassword(String userId, String password);

  /** 
   * Passes the authenticated user id for this particular thread.
   * All service method (from any service) invocations done by the same
   * thread will have access to this authenticatedUserId. 
   */
  void setAuthenticatedUserId(String authenticatedUserId);
  
  /** Sets the picture for a given user.
   * @throws ActivitiObjectNotFoundException if the user doesn't exist.
   * @param picture can be null to delete the picture. */
  void setUserPicture(String userId, Picture picture);

  /** Retrieves the picture for a given user.
   * @throws ActivitiObjectNotFoundException if the user doesn't exist.
   * @returns null if the user doesn't have a picture. */
  Picture getUserPicture(String userId);

  /** Generic extensibility key-value pairs associated with a user */
  void setUserInfo(String userId, String key, String value);
  
  /** Generic extensibility key-value pairs associated with a user */
  String getUserInfo(String userId, String key);

  /** Generic extensibility keys associated with a user */
  List<String> getUserInfoKeys(String userId);

  /** Delete an entry of the generic extensibility key-value pairs associated with a user */
  void deleteUserInfo(String userId, String key);
}
