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

package org.activiti.engine.test.api.identity;

import java.util.List;

import junit.framework.Assert;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;

/**
 * @author Frederik Heremans
 */
public class IdentityServiceTest extends ActivitiInternalTestCase {

  public void testCreateExistingUser() {
    User user = identityService.newUser("testuser");
    identityService.saveUser(user);
    try {
      User secondUser = identityService.newUser("testuser");
      identityService.saveUser(secondUser);
      fail("Exception should have been thrown");
    } catch (RuntimeException re) {
      // Expected exception while saving new user with the same name as an existing one.
    } 
      
    identityService.deleteUser(user.getId());
  }
  
  public void testUpdateUser() {
    // First, create a new user
    User user = identityService.newUser("johndoe");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("johndoe@alfresco.com");
    identityService.saveUser(user);

    // Fetch and update the user
    user = identityService.findUser("johndoe");
    user.setEmail("updated@alfresco.com");
    user.setFirstName("Jane");
    user.setLastName("Donnel");
    identityService.saveUser(user);

    user = identityService.findUser("johndoe");
    assertEquals("Jane", user.getFirstName());
    assertEquals("Donnel", user.getLastName());
    assertEquals("updated@alfresco.com", user.getEmail());

    identityService.deleteUser(user.getId());
  }

  public void testUpdateGroup() {
    Group group = identityService.newGroup("sales");
    group.setName("Sales");
    identityService.saveGroup(group);

    group = identityService.findGroupById("sales");
    group.setName("Updated");
    identityService.saveGroup(group);

    group = identityService.findGroupById("sales");
    Assert.assertEquals("Updated", group.getName());

    identityService.deleteGroup(group.getId());
  }

  public void findUserByUnexistingId() {
    User user = identityService.findUser("unexistinguser");
    Assert.assertNull(user);
  }

  public void findGroupByUnexistingId() {
    Group group = identityService.findGroupById("unexistinggroup");
    Assert.assertNull(group);
  }



  public void testCreateMembershipUnexistingGroup() {
    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);
    
    try {
      identityService.createMembership(johndoe.getId(), "unexistinggroup");
      fail("Expected exception");
    } catch(RuntimeException re) {
      // Exception expected
    }
    
    identityService.deleteUser(johndoe.getId());
  }
  
  public void testCreateMembershipUnexistingUser() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);
    
    try {
      identityService.createMembership("unexistinguser", sales.getId());
      fail("Expected exception");
    } catch(RuntimeException re) {
      // Exception expected
    }
    
    identityService.deleteGroup(sales.getId());
  }
  
  public void testCreateMembershipAlreadyExisting() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);
    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);
    
    // Create the membership
    identityService.createMembership(johndoe.getId(), sales.getId());
    
    try {
      identityService.createMembership(johndoe.getId(), sales.getId());      
    } catch(RuntimeException re) {
     // Expected exception, membership already exists
    }
    
    identityService.deleteGroup(sales.getId());
    identityService.deleteUser(johndoe.getId());
  }

  public void testSaveGroupNullArgument() {
    try {
      identityService.saveGroup(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("group is null", ae.getMessage());
    }
  }

  public void testSaveUserNullArgument() {
    try {
      identityService.saveUser(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("user is null", ae.getMessage());
    }
  }

  public void testFindUserNullArgument() {
    try {
      identityService.findUser(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId is null", ae.getMessage());
    }
  }

  public void testFindGroupByIdNullArgument() {
    try {
      identityService.findGroupById(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("groupId is null", ae.getMessage());
    }
  }

  public void testCreateMembershipNullArguments() {
    try {
      identityService.createMembership(null, "group");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId is null", ae.getMessage());
    }

    try {
      identityService.createMembership("userId", null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("groupId is null", ae.getMessage());
    }
  }

  public void testFindGroupsByUserIdNullArguments() {
    try {
      identityService.findGroupsByUserId(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId is null", ae.getMessage());
    }
  }

  public void testFindGroupsByUserIdAndGroupTypeNullArguments() {
    try {
      identityService.findGroupsByUserIdAndGroupType(null, null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId is null", ae.getMessage());
    }
  }
  
  public void testFindGroupsByUserIdAndTypeNullGroupType() {
    // Create user and add to 2 groups with a different type
    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);
    
    Group sales = identityService.newGroup("sales");
    sales.setType("type1");
    identityService.saveGroup(sales);
    
    Group admin = identityService.newGroup("admin");
    admin.setType("type2");
    identityService.saveGroup(admin);
    
    identityService.createMembership(johndoe.getId(), sales.getId());
    identityService.createMembership(johndoe.getId(), admin.getId());
    
    // When null is passed as groupTypes, groups of all types should be returned
    List<Group> groups = identityService.findGroupsByUserIdAndGroupType(johndoe.getId(), null);
    Assert.assertNotNull(groups);
    Assert.assertEquals(2, groups.size());
    
    identityService.deleteUser(johndoe.getId());
    identityService.deleteGroup(sales.getId());
    identityService.deleteGroup(admin.getId());
    
  }

  public void testFindUsersByGroupIdNullArguments() {
    try {
      identityService.findUsersByGroupId(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("groupId is null", ae.getMessage());
    }
  }

  public void testFindUsersByGroupUnexistingGroup() {
    List<User> users = identityService.findUsersByGroupId("unexistinggroup");
    Assert.assertNotNull(users);
    Assert.assertTrue(users.isEmpty());
  }

  public void testDeleteGroupNullArguments() {
    try {
      identityService.deleteGroup(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("groupId is null", ae.getMessage());
    }
  }

  public void testDeleteMembership() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);

    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);
    // Add membership
    identityService.createMembership(johndoe.getId(), sales.getId());

    List<Group> groups = identityService.findGroupsByUserId(johndoe.getId());
    Assert.assertTrue(groups.size() == 1);
    Assert.assertEquals("sales", groups.get(0).getId());

    // Delete the membership and check members of sales group
    identityService.deleteMembership(johndoe.getId(), sales.getId());
    groups = identityService.findGroupsByUserId(johndoe.getId());
    Assert.assertTrue(groups.size() == 0);

    identityService.deleteGroup("sales");
    identityService.deleteUser("johndoe");
  }
  
  public void testDeleteMembershipWhenUserIsNoMember() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);

    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);
    
    // Delete the membership when the user is no member
    identityService.deleteMembership(johndoe.getId(), sales.getId());
    
    identityService.deleteGroup("sales");
    identityService.deleteUser("johndoe");
  }
  
  public void testDeleteMembershipUnexistingGroup() {
    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);
    // No exception should be thrown when group doesn't exist
    identityService.deleteMembership(johndoe.getId(), "unexistinggroup");
    identityService.deleteUser(johndoe.getId());
  }
  
  public void testDeleteMembershipUnexistingUser() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);
    // No exception should be thrown when user doesn't exist
    identityService.deleteMembership("unexistinguser", sales.getId());
    identityService.deleteGroup(sales.getId());
  }
  
  public void testDeleteMemberschipNullArguments() {
    try {
      identityService.deleteMembership(null, "group");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId is null", ae.getMessage());
    }

    try {
      identityService.deleteMembership("user", null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("groupId is null", ae.getMessage());
    }
  }

  public void testDeleteUserNullArguments() {
    try {
      identityService.deleteUser(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId is null", ae.getMessage());
    }
  }

  public void testCheckPasswordNullSafe() {
    Assert.assertFalse(identityService.checkPassword("userId", null));
    Assert.assertFalse(identityService.checkPassword(null, "passwd"));
    Assert.assertFalse(identityService.checkPassword(null, null));
  }
}
