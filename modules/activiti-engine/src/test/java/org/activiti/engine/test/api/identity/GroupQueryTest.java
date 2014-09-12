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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

import java.util.List;


/**
 * @author Joram Barrez
 */
public class GroupQueryTest extends PluggableActivitiTestCase {
  
  protected void setUp() throws Exception {
    super.setUp();
    
    createGroup("muppets", "Muppet show characters", "user");
    createGroup("frogs", "Famous frogs", "user");
    createGroup("mammals", "Famous mammals from eighties", "user");
    createGroup("admin", "Administrators", "security");
    
    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("fozzie"));
    identityService.saveUser(identityService.newUser("mispiggy"));

    identityService.createMembership("kermit", "muppets");
    identityService.createMembership("fozzie", "muppets");
    identityService.createMembership("mispiggy", "muppets");
    
    identityService.createMembership("kermit", "frogs");
    
    identityService.createMembership("fozzie", "mammals");
    identityService.createMembership("mispiggy", "mammals");
    
    identityService.createMembership("kermit", "admin");

  }
  
  private Group createGroup(String id, String name, String type) {
    Group group = identityService.newGroup(id);
    group.setName(name);
    group.setType(type);
    identityService.saveGroup(group);
    return group;
  }
  
  @Override
  protected void tearDown() throws Exception {
    identityService.deleteUser("kermit");
    identityService.deleteUser("fozzie");
    identityService.deleteUser("mispiggy");
    
    identityService.deleteGroup("muppets");
    identityService.deleteGroup("mammals");
    identityService.deleteGroup("frogs");
    identityService.deleteGroup("admin");

    super.tearDown();
  }
  
  public void testQueryById() {
    GroupQuery query = identityService.createGroupQuery().groupId("muppets");
    verifyQueryResults(query, 1);
  }
  
  public void testQueryByInvalidId() {
    GroupQuery query = identityService.createGroupQuery().groupId("invalid");
    verifyQueryResults(query, 0);
    
    try {
      identityService.createGroupQuery().groupId(null).list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {}
  }
  
  public void testQueryByName() {
    GroupQuery query = identityService.createGroupQuery().groupName("Muppet show characters");
    verifyQueryResults(query, 1);
    
    query = identityService.createGroupQuery().groupName("Famous frogs");
    verifyQueryResults(query, 1);
  }
  
  public void testQueryByInvalidName() {
    GroupQuery query = identityService.createGroupQuery().groupName("invalid");
    verifyQueryResults(query, 0);
    
    try {
      identityService.createGroupQuery().groupName(null).list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {}
  }
  
  public void testQueryByNameLike() {
    GroupQuery query = identityService.createGroupQuery().groupNameLike("%Famous%");
    verifyQueryResults(query, 2);
    
    query = identityService.createGroupQuery().groupNameLike("Famous%");
    verifyQueryResults(query, 2);
    
    query = identityService.createGroupQuery().groupNameLike("%show%");
    verifyQueryResults(query, 1);
  }
  
  public void testQueryByInvalidNameLike() {
    GroupQuery query = identityService.createGroupQuery().groupNameLike("%invalid%");
    verifyQueryResults(query, 0);
    
    try {
      identityService.createGroupQuery().groupNameLike(null).list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {}
  }
  
  public void testQueryByType() {
    GroupQuery query = identityService.createGroupQuery().groupType("user");
    verifyQueryResults(query, 3);
    
    query = identityService.createGroupQuery().groupType("admin");
    verifyQueryResults(query, 0);
  }
  
  public void testQueryByInvalidType() {
    GroupQuery query = identityService.createGroupQuery().groupType("invalid");
    verifyQueryResults(query, 0);
    
    try {
      identityService.createGroupQuery().groupType(null).list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {}
  }
  
  public void testQueryByMember() {
    GroupQuery query = identityService.createGroupQuery().groupMember("fozzie");
    verifyQueryResults(query, 2);
    
    query = identityService.createGroupQuery().groupMember("kermit");
    verifyQueryResults(query, 3);
    
    query = query.orderByGroupId().asc();
    List<Group> groups = query.list();
    assertEquals(3, groups.size());
    assertEquals("admin", groups.get(0).getId());
    assertEquals("frogs", groups.get(1).getId());
    assertEquals("muppets", groups.get(2).getId());

    query = query.groupType("user");
    groups = query.list();
    assertEquals(2, groups.size());
    assertEquals("frogs", groups.get(0).getId());
    assertEquals("muppets", groups.get(1).getId());
  }
  
  public void testQueryByInvalidMember() {
    GroupQuery query = identityService.createGroupQuery().groupMember("invalid");
    verifyQueryResults(query, 0);
    
    try {
      identityService.createGroupQuery().groupMember(null).list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {}
  }
  
  public void testQuerySorting() {
    // asc
    assertEquals(4, identityService.createGroupQuery().orderByGroupId().asc().count());
    assertEquals(4, identityService.createGroupQuery().orderByGroupName().asc().count());
    assertEquals(4, identityService.createGroupQuery().orderByGroupType().asc().count());

    // desc
    assertEquals(4, identityService.createGroupQuery().orderByGroupId().desc().count());
    assertEquals(4, identityService.createGroupQuery().orderByGroupName().desc().count());
    assertEquals(4, identityService.createGroupQuery().orderByGroupType().desc().count());
    
    // Multiple sortings
    GroupQuery query = identityService.createGroupQuery().orderByGroupType().asc().orderByGroupName().desc();
    List<Group> groups = query.list();
    assertEquals(4, query.count());
    
    assertEquals("security", groups.get(0).getType());
    assertEquals("user", groups.get(1).getType());
    assertEquals("user", groups.get(2).getType());
    assertEquals("user", groups.get(3).getType());

    assertEquals("admin", groups.get(0).getId());
    assertEquals("muppets", groups.get(1).getId());
    assertEquals("mammals", groups.get(2).getId());
    assertEquals("frogs", groups.get(3).getId());
  }
  
  public void testQueryInvalidSortingUsage() {
    try {
      identityService.createGroupQuery().orderByGroupId().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {}
    
    try {
      identityService.createGroupQuery().orderByGroupId().orderByGroupName().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {}
  }
  
  private void verifyQueryResults(GroupQuery query, int countExpected) {
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
  
  private void verifySingleResultFails(GroupQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {}
  }

  public void testNativeQuery() {
    assertEquals("ACT_ID_GROUP", managementService.getTableName(Group.class));
    assertEquals("ACT_ID_GROUP", managementService.getTableName(GroupEntity.class));
    String tableName = managementService.getTableName(Group.class);
    String baseQuerySql = "SELECT * FROM " + tableName;

    assertEquals(4, identityService.createNativeGroupQuery().sql(baseQuerySql).list().size());

    assertEquals(1, identityService.createNativeGroupQuery().sql(baseQuerySql + " where ID_ = #{id}")
        .parameter("id", "admin").list().size());

    assertEquals(3, identityService.createNativeGroupQuery().sql("SELECT aig.* from " + tableName + " aig"
        + " inner join ACT_ID_MEMBERSHIP aim on aig.ID_ = aim.GROUP_ID_ "
        + " inner join ACT_ID_USER aiu on aiu.ID_ = aim.USER_ID_ where aiu.ID_ = #{id}")
        .parameter("id", "kermit").list().size());

    // paging
    assertEquals(2, identityService.createNativeGroupQuery().sql(baseQuerySql).listPage(0, 2).size());
    assertEquals(3, identityService.createNativeGroupQuery().sql(baseQuerySql).listPage(1, 3).size());
    assertEquals(1, identityService.createNativeGroupQuery().sql(baseQuerySql + " where ID_ = #{id}")
        .parameter("id", "admin").listPage(0, 1).size());
  }

}
