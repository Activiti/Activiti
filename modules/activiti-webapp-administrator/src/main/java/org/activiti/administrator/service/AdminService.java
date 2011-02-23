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
package org.activiti.administrator.service;

import java.util.Set;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;

/**
 * Administration service interface
 * 
 * @author Patrick Oberg
 * 
 */
public interface AdminService {

  BeanContainer<String, Group> getGroups();

  void refreshGroups();

  BeanContainer<String, User> getUsers();

  void refreshUsers();

  Set<String> getGroupMembers(String groupId);

  Set<String> getUserGroups(String userId);

  void updateUserGroups(String user, Set<String> groups);

  Set<String> getAssignedTaskIds(String id);

  Set<String> getUnassignedTaskIds(String id);

  void deleteUser(String id);

  void saveUser(Item user, Set<String> groups);

  Set<String> getUnassignedTaskIdsByGroup(String id);

  void deleteGroup(String id);

  void saveGroup(Item group, Set<String> groups);

  void updateGroupMembers(String id, Set<String> users);

  BeanItem<Group> newGroup();

  BeanItem<User> newUser();

}
