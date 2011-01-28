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

package org.activiti.standalone.cfg.identity;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.test.AbstractActivitiTestCase;

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class CustomIdenstitySessionTest extends AbstractActivitiTestCase {

  @Override
  protected void initializeProcessEngine() {
    processEngine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/activiti/standalone/cfg/identity/customIdentitySession-activiti.cfg.xml")
      .buildProcessEngine();
  }

  public void testAuthentication() {
    User user = identityService.newUser("johndoe");
    user.setPassword("xxx");
    identityService.saveUser(user);

    assertTrue(identityService.checkPassword("johndoe", "xxx"));
    assertFalse(identityService.checkPassword("johndoe", "invalid pwd"));

    identityService.deleteUser("johndoe");
  }
  
}
