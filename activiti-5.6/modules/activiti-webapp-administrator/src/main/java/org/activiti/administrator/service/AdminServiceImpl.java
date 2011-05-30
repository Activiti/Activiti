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

import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import org.activiti.engine.IdentityService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.task.Task;

/**
 * Provides access to the activiti services.
 * 
 * @author Patrick Oberg
 * 
 */
@Service(value = "adminService")
@Scope(value = "session")
public class AdminServiceImpl implements AdminService {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TaskService taskService;

  @Autowired
  private IdentityService identityService;

  // Holds all available groups
  private BeanContainer<String, Group> groups;

  // Holds all available users
  private BeanContainer<String, User> users;

  /* User related methods */

  /**
   * Get all users
   * 
   * Return all users
   * 
   * @return the users
   */
  public BeanContainer<String, User> getUsers() {
    if (users == null) {
      refreshUsers();
    }
    return users;
  }

  /**
   * Adds all users to the bean container
   * 
   * @return the users
   */
  public void refreshUsers() {

    users = new BeanContainer<String, User>(User.class);

    for (User user : getIdentityService().createUserQuery().orderByUserId().asc().list()) {

      // Add user id as key and user as value
      users.addItem(user.getId(), user);

    }

  }

  /**
   * Create new User Object wrapped in a bean item
   */
  public BeanItem<User> newUser() {

    // Get group
    return new BeanItem<User>(new UserEntity());

  }

  /**
   * Delete the user
   * 
   * @param id
   *          the User Id
   */
  public void deleteUser(String id) {
    getIdentityService().deleteUser(id);
  }

  /**
   * Save the user
   * 
   * @param item
   *          the User Item
   */
  public void saveUser(Item item, Set<String> groups) {

    User user = null;

    if (hasUser(item.getItemProperty("id").toString())) {
      // Get user from data source
      user = getUser(item.getItemProperty("id").toString());
    } else {
      user = new UserEntity();
    }

    // Update the fields
    user.setId(item.getItemProperty("id").toString());
    user.setPassword(item.getItemProperty("password").toString());
    user.setEmail(item.getItemProperty("email").toString());
    user.setFirstName(item.getItemProperty("firstName").toString());
    user.setLastName(item.getItemProperty("lastName").toString());

    // Save the user
    getIdentityService().saveUser(user);

    // Refresh the user from db
    user = getUser(user.getId());

    // Refresh user list
    getUsers().removeItem(user.getId());

    // Add the new/updated user to the user list
    getUsers().addItem(user.getId(), user);

    // Update user groups
    updateUserGroups(user.getId(), groups);

  }

  /* Group related methods */

  /**
   * Get all groups
   * 
   * @return the groups
   */
  public BeanContainer<String, Group> getGroups() {
    if (groups == null) {
      refreshGroups();
    }
    return groups;
  }

  /**
   * Adds all groups to the bean container
   * 
   * @return the groups
   */
  public void refreshGroups() {

    groups = new BeanContainer<String, Group>(Group.class);

    for (Group group : getIdentityService().createGroupQuery().orderByGroupId().asc().list()) {

      // Add group id as key and group as value
      groups.addItem(group.getId(), group);
    }
  }

  /**
   * Get all user ids of the specified group
   * 
   * @param groupId
   *          the id of the group
   * @return the user ids of the group
   */
  public Set<String> getGroupMembers(String groupId) {

    HashSet<String> members = new HashSet<String>();

    for (User user : getIdentityService().createUserQuery().memberOfGroup(groupId).list()) {
      members.add(user.getId());
    }

    return members;
  }

  /**
   * Get all group ids for the user
   * 
   * @param userId
   *          the id of the user
   * @return the groups
   */
  public Set<String> getUserGroups(String userId) {

    HashSet<String> groups = new HashSet<String>();

    for (Group group : getIdentityService().createGroupQuery().groupMember(userId).list()) {
      groups.add(group.getId());
    }

    return groups;
  }

  /**
   * Update the groups of the user
   * 
   * @param user
   *          the user
   * @param groups
   *          the userGroups
   */
  public void updateUserGroups(String userId, Set<String> groups) {

    // Get user groups
    Set<String> userGroups = getUserGroups(userId);

    // Remove old user groups
    for (String group : userGroups) {
      if (!groups.contains(group))
        getIdentityService().deleteMembership(userId, group);
    }

    // Get updated group members
    userGroups = getUserGroups(userId);

    // Add new user groups
    for (String group : groups) {
      if (!userGroups.contains(group))
        getIdentityService().createMembership(userId, group);
    }

  }

  /**
   * Delete the group
   * 
   * @param id
   *          the user id
   */
  public void deleteGroup(String id) {
    getIdentityService().deleteGroup(id);
  }

  /**
   * Group the user
   * 
   * @param item
   *          the user item
   */
  public void saveGroup(Item item, Set<String> users) {

    Group group = null;

    if (hasGroup(item.getItemProperty("id").toString())) {
      // Get group from data source
      group = getGroup(item.getItemProperty("id").toString());
    } else {
      group = new GroupEntity();
    }

    // Update the fields
    group.setId(item.getItemProperty("id").toString());
    group.setName(item.getItemProperty("name").toString());
    group.setType(item.getItemProperty("type").toString());

    // Save the group
    getIdentityService().saveGroup(group);

    // Refresh the group from db
    group = getGroup(group.getId());

    // Refresh group list
    getGroups().removeItem(group.getId());

    // Add the new/updated group to the group list
    getGroups().addItem(group.getId(), group);

    // Update user groups
    updateGroupMembers(group.getId(), users);

  }

  /**
   * Update the group members
   * 
   * @param id
   *          the group id
   * 
   * @param users
   *          the user of the group
   */
  public void updateGroupMembers(String id, Set<String> users) {

    // Get group members
    Set<String> members = getGroupMembers(id);

    // Remove old group members
    for (String user : members) {
      if (!users.contains(user))
        getIdentityService().deleteMembership(user, id);
    }

    // Get updated group members
    members = getGroupMembers(id);

    // Add new group members
    for (String user : users) {
      if (!members.contains(user))
        getIdentityService().createMembership(user, id);
    }

  }

  /**
   * Create new Group Object wrapped in a bean item
   */
  public BeanItem<Group> newGroup() {

    // Get group
    return new BeanItem<Group>(new GroupEntity());

  }

  /* Task related methods */

  /**
   * Get all tasks assigned to the user
   * 
   * @param id
   *          the User Id
   * 
   * @return the assignTasks
   */
  public Set<String> getAssignedTaskIds(String id) {

    Set<String> assignedTasks = new HashSet<String>();

    for (Task task : getTaskService().createTaskQuery().taskAssignee(id).orderByTaskCreateTime().desc().list()) {
      assignedTasks.add(task.getId());
    }

    // Return unassigned tasks
    return assignedTasks;

  }

  /**
   * Get all tasks pooled for the user
   * 
   * @param id
   *          the User Id
   * 
   * @return the unassignTasks
   */
  public Set<String> getUnassignedTaskIds(String id) {

    Set<String> unassignedTasks = new HashSet<String>();

    for (Task task : getTaskService().createTaskQuery().taskCandidateUser(id).orderByTaskCreateTime().desc().list()) {
      unassignedTasks.add(task.getId());
    }

    // Return unassigned tasks
    return unassignedTasks;

  }

  /**
   * Get all tasks pooled for the group
   * 
   * @param id
   *          the user id
   */
  public Set<String> getUnassignedTaskIdsByGroup(String id) {

    Set<String> unassignedTasks = new HashSet<String>();

    // Get all task ids
    for (Task task : getTaskService().createTaskQuery().taskCandidateGroup(id).orderByTaskCreateTime().desc().list()) {
      unassignedTasks.add(task.getId());
    }

    // Return unassigned tasks
    return unassignedTasks;

  }

  /**
   * Get the user
   * 
   * @param id
   *          the user id
   */
  private User getUser(String id) {

    // Get user
    return (User) getIdentityService().createUserQuery().userId(id).singleResult();

  }

  /**
   * Get the group
   * 
   * @param id
   *          the group id
   */
  private Group getGroup(String id) {

    // Get group
    return (Group) getIdentityService().createGroupQuery().groupId(id).singleResult();

  }

  /**
   * Checks if the user exists in the db
   * 
   * @param id
   *          the user id
   */
  private boolean hasUser(String id) {

    if (identityService.createUserQuery().userId(id).singleResult() != null)
      return true;
    else
      return false;
  }

  /**
   * Checks if the group exists in the db
   * 
   * @param id
   *          the group id
   */
  private boolean hasGroup(String id) {

    if (identityService.createGroupQuery().groupId(id).singleResult() != null)
      return true;
    else
      return false;
  }

  /**
   * Get activiti identity service
   * 
   * @return the identityService
   */
  private IdentityService getIdentityService() {
    return identityService;
  }

  /**
   * Get activiti task service
   * 
   * @return the taskService
   */
  private TaskService getTaskService() {
    return taskService;
  }

}