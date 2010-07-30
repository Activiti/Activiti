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
  void saveUser(User user);
  User findUser(String userId);
  List<User> findUsersByGroup(String groupId);
  void deleteUser(String userId);

  Group newGroup(String groupId);
  void saveGroup(Group group);
  Group findGroup(String groupId);
  List<Group> findGroupsByUser(String userId);
  List<Group> findGroupsByUserAndType(String userId, String groupType);
  void deleteGroup(String groupId);

  void createMembership(String userId, String groupId);
  void deleteMembership(String userId, String groupId);

  boolean checkPassword(String userId, String password);
}
