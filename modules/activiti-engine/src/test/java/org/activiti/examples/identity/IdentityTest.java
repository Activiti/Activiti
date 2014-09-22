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
package org.activiti.examples.identity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

/**
 * @author Tom Baeyens
 */
public class IdentityTest extends PluggableActivitiTestCase {

  public void testAuthentication() {
    User user = identityService.newUser("johndoe");
    user.setPassword("xxx");
    identityService.saveUser(user);

    assertTrue(identityService.checkPassword("johndoe", "xxx"));
    assertFalse(identityService.checkPassword("johndoe", "invalid pwd"));

    identityService.deleteUser("johndoe");
  }

  public void testFindGroupsByUserAndType() {
    Group sales = identityService.newGroup("sales");
    sales.setType("hierarchy");
    identityService.saveGroup(sales);

    Group development = identityService.newGroup("development");
    development.setType("hierarchy");
    identityService.saveGroup(development);

    Group admin = identityService.newGroup("admin");
    admin.setType("security-role");
    identityService.saveGroup(admin);

    Group user = identityService.newGroup("user");
    user.setType("security-role");
    identityService.saveGroup(user);

    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);

    User joesmoe = identityService.newUser("joesmoe");
    identityService.saveUser(joesmoe);

    User jackblack = identityService.newUser("jackblack");
    identityService.saveUser(jackblack);

    identityService.createMembership("johndoe", "sales");
    identityService.createMembership("johndoe", "user");
    identityService.createMembership("johndoe", "admin");

    identityService.createMembership("joesmoe", "user");

    List<Group> groups = identityService.createGroupQuery().groupMember("johndoe").groupType("security-role").list();
    Set<String> groupIds = getGroupIds(groups);
    Set<String> expectedGroupIds = new HashSet<String>();
    expectedGroupIds.add("user");
    expectedGroupIds.add("admin");
    assertEquals(expectedGroupIds, groupIds);

    groups = identityService.createGroupQuery().groupMember("joesmoe").groupType("security-role").list();
    groupIds = getGroupIds(groups);
    expectedGroupIds = new HashSet<String>();
    expectedGroupIds.add("user");
    assertEquals(expectedGroupIds, groupIds);

    groups = identityService.createGroupQuery().groupMember("jackblack").groupType("security-role").list();
    assertTrue(groups.isEmpty());

    identityService.deleteGroup("sales");
    identityService.deleteGroup("development");
    identityService.deleteGroup("admin");
    identityService.deleteGroup("user");
    identityService.deleteUser("johndoe");
    identityService.deleteUser("joesmoe");
    identityService.deleteUser("jackblack");
  }

  public void testUser() {
    User user = identityService.newUser("johndoe");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("johndoe@alfresco.com");
    identityService.saveUser(user);

    user = identityService.createUserQuery().userId("johndoe").singleResult();
    assertEquals("johndoe", user.getId());
    assertEquals("John", user.getFirstName());
    assertEquals("Doe", user.getLastName());
    assertEquals("johndoe@alfresco.com", user.getEmail());

    identityService.deleteUser("johndoe");
  }

  public void testGroup() {
    Group group = identityService.newGroup("sales");
    group.setName("Sales division");
    identityService.saveGroup(group);

    group = identityService.createGroupQuery().groupId("sales").singleResult();
    assertEquals("sales", group.getId());
    assertEquals("Sales division", group.getName());

    identityService.deleteGroup("sales");
  }

  public void testMembership() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);

    Group development = identityService.newGroup("development");
    identityService.saveGroup(development);

    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);

    User joesmoe = identityService.newUser("joesmoe");
    identityService.saveUser(joesmoe);

    User jackblack = identityService.newUser("jackblack");
    identityService.saveUser(jackblack);

    identityService.createMembership("johndoe", "sales");
    identityService.createMembership("joesmoe", "sales");

    identityService.createMembership("joesmoe", "development");
    identityService.createMembership("jackblack", "development");

    List<Group> groups = identityService.createGroupQuery().groupMember("johndoe").list();
    assertEquals(createStringSet("sales"), getGroupIds(groups));

    groups = identityService.createGroupQuery().groupMember("joesmoe").list();
    assertEquals(createStringSet("sales", "development"), getGroupIds(groups));

    groups = identityService.createGroupQuery().groupMember("jackblack").list();
    assertEquals(createStringSet("development"), getGroupIds(groups));

    List<User> users = identityService.createUserQuery().memberOfGroup("sales").list();
    assertEquals(createStringSet("johndoe", "joesmoe"), getUserIds(users));

    users = identityService.createUserQuery().memberOfGroup("development").list();
    assertEquals(createStringSet("joesmoe", "jackblack"), getUserIds(users));

    identityService.deleteGroup("sales");
    identityService.deleteGroup("development");

    identityService.deleteUser("jackblack");
    identityService.deleteUser("joesmoe");
    identityService.deleteUser("johndoe");
  }

  private Object createStringSet(String... strings) {
    Set<String> stringSet = new HashSet<String>();
    Collections.addAll(stringSet, strings);
    return stringSet;
  }

  public Set<String> getGroupIds(List<Group> groups) {
    Set<String> groupIds = new HashSet<String>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }
  public Set<String> getUserIds(List<User> users) {
    Set<String> userIds = new HashSet<String>();
    for (User user : users) {
      userIds.add(user.getId());
    }
    return userIds;
  }
}
