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

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;

public class GroupQueryEscapeClauseTest extends AbstractEscapeClauseTestCase {

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
