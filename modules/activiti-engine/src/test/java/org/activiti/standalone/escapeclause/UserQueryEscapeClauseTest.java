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
package org.activiti.standalone.escapeclause;

import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;

public class UserQueryEscapeClauseTest extends AbstractEscapeClauseTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    
    createUser("kermit", "Kermit%", "Thefrog%", "kermit%@muppetshow.com");
    createUser("fozzie", "Fozzie_", "Bear_", "fozzie_@muppetshow.com");
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
    
    super.tearDown();
  }
  
  public void testQueryByFirstNameLike() {
    UserQuery query = identityService.createUserQuery().userFirstNameLike("%\\%%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("kermit", query.singleResult().getId());
    
    query = identityService.createUserQuery().userFirstNameLike("%\\_%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("fozzie", query.singleResult().getId());
  }
  
  public void testQueryByLastNameLike() {
    UserQuery query = identityService.createUserQuery().userLastNameLike("%\\%%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("kermit", query.singleResult().getId());
    
    query = identityService.createUserQuery().userLastNameLike("%\\_%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("fozzie", query.singleResult().getId());
  }

  public void testQueryByFullNameLike() {
    UserQuery query = identityService.createUserQuery().userFullNameLike("%og\\%%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("kermit", query.singleResult().getId());
    
    query = identityService.createUserQuery().userFullNameLike("%it\\%%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("kermit", query.singleResult().getId());
    
    query = identityService.createUserQuery().userFullNameLike("%ar\\_%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("fozzie", query.singleResult().getId());
    
    query = identityService.createUserQuery().userFullNameLike("%ie\\_%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("fozzie", query.singleResult().getId());
  }

  public void testQueryByEmailLike() {
    UserQuery query = identityService.createUserQuery().userEmailLike("%\\%%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("kermit", query.singleResult().getId());
    
    query = identityService.createUserQuery().userEmailLike("%\\_%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("fozzie", query.singleResult().getId());
  }
}
