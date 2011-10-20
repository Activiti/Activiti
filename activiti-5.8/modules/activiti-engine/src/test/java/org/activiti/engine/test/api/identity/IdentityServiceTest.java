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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.identity.Account;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

/**
 * @author Frederik Heremans
 */
public class IdentityServiceTest extends PluggableActivitiTestCase {

  public void testUserInfo() {
    User user = identityService.newUser("testuser");
    identityService.saveUser(user);
    
    identityService.setUserInfo("testuser", "myinfo", "myvalue");
    assertEquals("myvalue", identityService.getUserInfo("testuser", "myinfo"));
    
    identityService.setUserInfo("testuser", "myinfo", "myvalue2");
    assertEquals("myvalue2", identityService.getUserInfo("testuser", "myinfo"));
    
    identityService.deleteUserInfo("testuser", "myinfo");
    assertNull(identityService.getUserInfo("testuser", "myinfo"));
    
    identityService.deleteUser(user.getId());
  }
  
  public void testUserAccount() {
    User user = identityService.newUser("testuser");
    identityService.saveUser(user);
    
    identityService.setUserAccount("testuser", "123", "google", "mygoogleusername", "mygooglepwd", null);
    Account googleAccount = identityService.getUserAccount("testuser", "123", "google");
    assertEquals("google", googleAccount.getName());
    assertEquals("mygoogleusername", googleAccount.getUsername());
    assertEquals("mygooglepwd", googleAccount.getPassword());
    
    identityService.setUserAccount("testuser", "123", "google", "mygoogleusername2", "mygooglepwd2", null);
    googleAccount = identityService.getUserAccount("testuser", "123", "google");
    assertEquals("google", googleAccount.getName());
    assertEquals("mygoogleusername2", googleAccount.getUsername());
    assertEquals("mygooglepwd2", googleAccount.getPassword());

    identityService.setUserAccount("testuser", "123", "alfresco", "myalfrescousername", "myalfrescopwd", null);
    identityService.setUserInfo("testuser", "myinfo", "myvalue");
    identityService.setUserInfo("testuser", "myinfo2", "myvalue2");

    List<String> expectedUserAccountNames = new ArrayList<String>();
    expectedUserAccountNames.add("google");
    expectedUserAccountNames.add("alfresco");
    List<String> userAccountNames = identityService.getUserAccountNames("testuser");
    assertListElementsMatch(expectedUserAccountNames, userAccountNames);
    
    identityService.deleteUserAccount("testuser", "google");
    
    expectedUserAccountNames.remove("google");
    
    userAccountNames = identityService.getUserAccountNames("testuser");
    assertListElementsMatch(expectedUserAccountNames, userAccountNames);
    
    identityService.deleteUser(user.getId());
  }
  
  private void assertListElementsMatch(List<String> list1, List<String> list2) {
    if(list1 != null) {
      assertNotNull(list2);
      assertEquals(list1.size(), list2.size());
      for (String value : list1) {
        assertTrue(list2.contains(value));
      }
    } else {
      assertNull(list2);
    }
    
  }

  public void testUserAccountDetails() {
    User user = identityService.newUser("testuser");
    identityService.saveUser(user);
    
    Map<String, String> accountDetails = new HashMap<String, String>();
    accountDetails.put("server", "localhost");
    accountDetails.put("port", "35");
    identityService.setUserAccount("testuser", "123", "google", "mygoogleusername", "mygooglepwd", accountDetails);
    Account googleAccount = identityService.getUserAccount("testuser", "123", "google");
    assertEquals(accountDetails, googleAccount.getDetails());
    
    identityService.deleteUser(user.getId());
  }
  
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
    user = identityService.createUserQuery().userId("johndoe").singleResult();
    user.setEmail("updated@alfresco.com");
    user.setFirstName("Jane");
    user.setLastName("Donnel");
    identityService.saveUser(user);

    user = identityService.createUserQuery().userId("johndoe").singleResult();
    assertEquals("Jane", user.getFirstName());
    assertEquals("Donnel", user.getLastName());
    assertEquals("updated@alfresco.com", user.getEmail());

    identityService.deleteUser(user.getId());
  }

  public void testUserPicture() {
    // First, create a new user
    User user = identityService.newUser("johndoe");
    identityService.saveUser(user);
    String userId = user.getId();

    Picture picture = new Picture("niceface".getBytes(), "image/string");
    identityService.setUserPicture(userId, picture);
    
    picture = identityService.getUserPicture(userId);

    // Fetch and update the user
    user = identityService.createUserQuery().userId("johndoe").singleResult();
    assertTrue("byte arrays differ", Arrays.equals("niceface".getBytes(), picture.getBytes()));
    assertEquals("image/string", picture.getMimeType());

    identityService.deleteUser(user.getId());
  }

  public void testUpdateGroup() {
    Group group = identityService.newGroup("sales");
    group.setName("Sales");
    identityService.saveGroup(group);

    group = identityService.createGroupQuery().groupId("sales").singleResult();
    group.setName("Updated");
    identityService.saveGroup(group);

    group = identityService.createGroupQuery().groupId("sales").singleResult();
    assertEquals("Updated", group.getName());

    identityService.deleteGroup(group.getId());
  }

  public void findUserByUnexistingId() {
    User user = identityService.createUserQuery().userId("unexistinguser").singleResult();
    assertNull(user);
  }

  public void findGroupByUnexistingId() {
    Group group = identityService.createGroupQuery().groupId("unexistinggroup").singleResult();
    assertNull(group);
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
  
  public void testFindGroupByIdNullArgument() {
    try {
      identityService.createGroupQuery().groupId(null).singleResult();
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("id is null", ae.getMessage());
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
      identityService.createGroupQuery().groupMember(null).singleResult();
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId is null", ae.getMessage());
    }
  }

  public void testFindUsersByGroupUnexistingGroup() {
    List<User> users = identityService.createUserQuery().memberOfGroup("unexistinggroup").list();
    assertNotNull(users);
    assertTrue(users.isEmpty());
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

    List<Group> groups = identityService.createGroupQuery().groupMember(johndoe.getId()).list();
    assertTrue(groups.size() == 1);
    assertEquals("sales", groups.get(0).getId());

    // Delete the membership and check members of sales group
    identityService.deleteMembership(johndoe.getId(), sales.getId());
    groups = identityService.createGroupQuery().groupMember(johndoe.getId()).list();
    assertTrue(groups.size() == 0);

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
  
  public void testDeleteUserUnexistingUserId() {
    // No exception should be thrown. Deleting an unexisting user should
    // be ignored silently
     identityService.deleteUser("unexistinguser");
  }

  public void testCheckPasswordNullSafe() {
    assertFalse(identityService.checkPassword("userId", null));
    assertFalse(identityService.checkPassword(null, "passwd"));
    assertFalse(identityService.checkPassword(null, null));
  }
}
