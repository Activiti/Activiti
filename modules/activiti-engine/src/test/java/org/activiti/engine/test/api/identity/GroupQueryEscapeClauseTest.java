package org.activiti.engine.test.api.identity;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

public class GroupQueryEscapeClauseTest extends PluggableActivitiTestCase {

  protected void setUp() throws Exception {
  super.setUp();

  createGroup("muppets", "muppets%", "user");
  createGroup("frogs", "frogs_", "user");
  }
  
  @Override
  protected void tearDown() throws Exception {
    identityService.deleteGroup("muppets");
    identityService.deleteGroup("frogs");
    super.tearDown();
  }
  
  public void testQueryByNameLike() {
    GroupQuery query = identityService.createGroupQuery().groupNameLike("%\\%%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("muppets", query.singleResult().getId());
    
    query = identityService.createGroupQuery().groupNameLike("%\\_%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("frogs", query.singleResult().getId());
  }
  
  private Group createGroup(String id, String name, String type) {
    Group group = identityService.newGroup(id);
    group.setName(name);
    group.setType(type);
    identityService.saveGroup(group);
    return group;
  }
}
