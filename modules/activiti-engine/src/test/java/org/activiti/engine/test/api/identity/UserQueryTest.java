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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;


/**
 * @author Joram Barrez
 */
public class UserQueryTest extends ActivitiInternalTestCase {
  
  protected void setUp() throws Exception {
    super.setUp();
    
    createUser("kermit", "Kermit", "Thefrog", "kermit@muppetshow.com");
    createUser("fozzie", "Fozzie", "Bear", "fozzie@muppetshow.com");
    createUser("gonzo", "Gonzo", "The great", "gonzo@muppetshow.com");
    
    identityService.saveGroup(identityService.newGroup("muppets"));
    identityService.saveGroup(identityService.newGroup("frogs"));
    
    identityService.createMembership("kermit", "muppets");
    identityService.createMembership("kermit", "frogs");
    identityService.createMembership("fozzie", "muppets");
    identityService.createMembership("gonzo", "muppets");
  }
  
  private User createUser(String id, String firstName, String lastName, String email) {
    User user = identityService.newUser(id);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    identityService.saveUser(user);
    return user;
  }
  
  @Override
  protected void tearDown() throws Exception {
    identityService.deleteUser("kermit");
    identityService.deleteUser("fozzie");
    identityService.deleteUser("gonzo");

    identityService.deleteGroup("muppets");
    identityService.deleteGroup("frogs");
    
    super.tearDown();
  }
  
  public void testQueryByNoCriteria() {
    UserQuery query = identityService.createUserQuery();
    verifyQueryResults(query, 3);
  }
  
  public void testQueryById() {
    UserQuery query = identityService.createUserQuery().id("kermit");
    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidId() {
    UserQuery query = identityService.createUserQuery().id("invalid");
    verifyQueryResults(query, 0);
    
    try {
      identityService.createUserQuery().id(null).singleResult();
      fail();
    } catch (ActivitiException e) { }
  }
  
  public void testQueryByFirstName() {
    UserQuery query = identityService.createUserQuery().firstName("Gonzo");
    verifyQueryResults(query, 1);
    
    User result = query.singleResult();
    assertEquals("gonzo", result.getId());
  }
  
  public void testQueryByInvalidFirstName() {
    UserQuery query = identityService.createUserQuery().firstName("invalid");
    verifyQueryResults(query, 0);
    
    try {
      identityService.createUserQuery().firstName(null).singleResult();
      fail();
    } catch (ActivitiException e) { }
  }
  
  public void testQueryByFirstNameLike() {
    UserQuery query = identityService.createUserQuery().firstNameLike("%o%");
    verifyQueryResults(query, 2);
    
    query = identityService.createUserQuery().firstNameLike("Ker%");
    verifyQueryResults(query, 1);
  }
  
  public void testQueryByInvalidFirstNameLike() {
    UserQuery query = identityService.createUserQuery().firstNameLike("%mispiggy%");
    verifyQueryResults(query, 0);
    
    try {
      identityService.createUserQuery().firstNameLike(null).singleResult();
      fail();
    } catch (ActivitiException e) { }
  }
  
  public void testQueryByLastName() {
    UserQuery query = identityService.createUserQuery().lastName("Bear");
    verifyQueryResults(query, 1);
    
    User result = query.singleResult();
    assertEquals("fozzie", result.getId());
  }
  
  public void testQueryByInvalidLastName() {
    UserQuery query = identityService.createUserQuery().lastName("invalid");
    verifyQueryResults(query, 0);
      
    try {
      identityService.createUserQuery().lastName(null).singleResult();
      fail();
    } catch (ActivitiException e) { }
  }
  
  public void testQueryByLastNameLike() {
    UserQuery query = identityService.createUserQuery().lastNameLike("%rog%");
    verifyQueryResults(query, 1);
    
    query = identityService.createUserQuery().lastNameLike("%ea%");
    verifyQueryResults(query, 2);
  }
  
  public void testQueryByInvalidLastNameLike() {
    UserQuery query = identityService.createUserQuery().lastNameLike("%invalid%");
    verifyQueryResults(query, 0);
      
    try {
      identityService.createUserQuery().lastNameLike(null).singleResult();
      fail();
    } catch (ActivitiException e) { }
  }
  
  public void testQueryByEmail() {
    UserQuery query = identityService.createUserQuery().email("kermit@muppetshow.com");
    verifyQueryResults(query, 1);
  }
  
  public void testQueryByInvalidEmail() {
    UserQuery query = identityService.createUserQuery().email("invalid");
    verifyQueryResults(query, 0);
      
    try {
      identityService.createUserQuery().email(null).singleResult();
      fail();
    } catch (ActivitiException e) { }
  }
  
  public void testQueryByEmailLike() {
    UserQuery query = identityService.createUserQuery().emailLike("%muppetshow.com");
    verifyQueryResults(query, 3);
    
    query = identityService.createUserQuery().emailLike("%kermit%");
    verifyQueryResults(query, 1);
  }
  
  public void testQueryByInvalidEmailLike() {
    UserQuery query = identityService.createUserQuery().emailLike("%invalid%");
    verifyQueryResults(query, 0);
      
    try {
      identityService.createUserQuery().emailLike(null).singleResult();
      fail();
    } catch (ActivitiException e) { }
  }
  
  public void testQuerySorting() {
    // asc
    assertEquals(3, identityService.createUserQuery().orderById().asc().count());
    assertEquals(3, identityService.createUserQuery().orderByEmail().asc().count());
    assertEquals(3, identityService.createUserQuery().orderByFirstName().asc().count());
    assertEquals(3, identityService.createUserQuery().orderByLastName().asc().count());
    
    // desc
    assertEquals(3, identityService.createUserQuery().orderById().desc().count());
    assertEquals(3, identityService.createUserQuery().orderByEmail().desc().count());
    assertEquals(3, identityService.createUserQuery().orderByFirstName().desc().count());
    assertEquals(3, identityService.createUserQuery().orderByLastName().desc().count());

    // Combined with criteria
    UserQuery query = identityService.createUserQuery().lastNameLike("%ea%").orderByFirstName().asc();
    List<User> users = query.list();
    assertEquals(2,users.size());
    assertEquals("Fozzie", users.get(0).getFirstName());
    assertEquals("Gonzo", users.get(1).getFirstName());
  }
  
  public void testQueryInvalidSortingUsage() {
    try {
      identityService.createUserQuery().orderById().list();
      fail();
    } catch (ActivitiException e) {}
    
    try {
      identityService.createUserQuery().orderById().orderByEmail().list();
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQueryByMemberOf() {
    UserQuery query = identityService.createUserQuery().memberOfGroup("muppets");
    verifyQueryResults(query, 3);
    
    query = identityService.createUserQuery().memberOfGroup("frogs");
    verifyQueryResults(query, 1);
    
    User result = query.singleResult();
    assertEquals("kermit", result.getId());
  }
  
  public void testQueryByInvalidMemberOf() {
    UserQuery query = identityService.createUserQuery().memberOfGroup("invalid");
    verifyQueryResults(query, 0);
    
    try {
      identityService.createUserQuery().memberOfGroup(null).list();
      fail();
    } catch (ActivitiException e) { }
  }
  
  private void verifyQueryResults(UserQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());
    
    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }
  
  private void verifySingleResultFails(UserQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {}
  }
  
}
