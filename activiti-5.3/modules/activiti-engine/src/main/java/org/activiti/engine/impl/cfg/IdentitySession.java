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

package org.activiti.engine.impl.cfg;

import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;


/**
 * @author Tom Baeyens
 */
public interface IdentitySession {

  /* User */
  User createNewUser(String userId);
  void insertUser(User user);
  void updateUser(User updatedUser);
  void deleteUser(String userId);
  User findUserById(String userId);
  List<User> findUsersByGroupId(String groupId);
  boolean isValidUser(String userId);
  UserQuery createNewUserQuery();
  List<User> findUserByQueryCriteria(Object query, Page page);
  long findUserCountByQueryCriteria(Object query);
  
  /* Group */
  Group createNewGroup(String groupId);
  void insertGroup(Group group);
  void updateGroup(Group updatedGroup);
  void deleteGroup(String groupId);
  Group findGroupById(String groupId);
  List<Group> findGroupsByUser(String userId);
  GroupQuery createNewGroupQuery();
  List<Group> findGroupByQueryCriteria(Object query, Page page);
  long findGroupCountByQueryCriteria(Object query);

  /* Membership */
  void createMembership(String userId, String groupId);
  void deleteMembership(String userId, String groupId);


}
