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

import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;


/**
 * is the service to manage {@link User}s and {@link Group}s.
 * 
 * @author Tom Baeyens
 */
public interface IdentityService {

  User newUser(String userId);
  
  /**
   * @throws RuntimeException when a user with the same name already exists.
   */
  void saveUser(User user);
  
  /**
   * @return the user for the given id. Returns null if no user is found.
   */
  User findUser(String userId);
  
  /**
   * @return all users that are member of the given group. If no group exists with the 
   * given id, an empty list is returned.
   */
  List<User> findUsersByGroupId(String groupId);
  
  /**
   * @throws RuntimeException when no user is found with the given userId.
   */
  void deleteUser(String userId);
  
  Group newGroup(String groupId);
  
  /**
   * @throws RuntimeException when a group with the same name already exists.
   */
  void saveGroup(Group group);
  
  /**
   * @return the group with the given id. Returns null if no group is found.
   */
  Group findGroupById(String groupId);
  
  /**
   * @return all groups the user is a member of. 
   * Returns an empty list if the user is not a member of any groups.
   */
  List<Group> findGroupsByUserId(String userId);
  
  /**
   * @return all groups the user is a member of which are of the given type. 
   * Returns an empty list the user is not a member of a matching group.
   * 
   */
  List<Group> findGroupsByUserIdAndGroupType(String userId, String groupType);
  
  /**
   * @throws RuntimeException when there is no group with the given id.
   */
  void deleteGroup(String groupId);

  /**
   * @throws RuntimeException when the given user or group doesn't exist or when the user
   * is already member of the group.
   */
  void createMembership(String userId, String groupId);
  
  
  /**
   * When the group or user don't exist or when the user is not a member of the group, 
   * this operation is ignored.
   */
  void deleteMembership(String userId, String groupId);

  boolean checkPassword(String userId, String password);
}
