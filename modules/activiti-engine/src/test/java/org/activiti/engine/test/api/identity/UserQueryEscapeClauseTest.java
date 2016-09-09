package org.activiti.engine.test.api.identity;

import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

public class UserQueryEscapeClauseTest extends PluggableActivitiTestCase {

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
