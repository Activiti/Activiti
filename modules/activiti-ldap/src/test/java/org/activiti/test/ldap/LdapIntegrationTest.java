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
package org.activiti.test.ldap;

import org.activiti.engine.test.Deployment;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:activiti-context.xml")
public class LdapIntegrationTest extends LDAPTestCase {
  
  public void testAuthenticationThroughLdap() {
    assertTrue(identityService.checkPassword("kermit", "pass"));
    assertFalse(identityService.checkPassword("kermit", "blah"));
  }
  
  @Deployment
  public void testCandidateGroupFetchedThroughLdap() {
    runtimeService.startProcessInstanceByKey("testCandidateGroup");
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroup("Sales").count());

    // Pepe is a member of the candidate group and should be able to find the task
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser("pepe").count());
    
    // Kermit is a candidate user and should be able to find the task
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser("kermit").count());
    
  }
  
}
