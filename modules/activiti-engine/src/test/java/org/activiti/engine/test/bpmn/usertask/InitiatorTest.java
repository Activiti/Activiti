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

package org.activiti.engine.test.bpmn.usertask;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;


/**
 * @author Tom Baeyens
 */
public class InitiatorTest extends PluggableActivitiTestCase {

  @Deployment
  public void testInitiator() {
    try {
      identityService.setAuthenticatedUserId("bono");
      runtimeService.startProcessInstanceByKey("InitiatorProcess");
    } finally {
      identityService.setAuthenticatedUserId(null);
    }
    
    assertEquals(1, taskService.createTaskQuery().taskAssignee("bono").count());
  }
  
  // See ACT-1372
  @Deployment
  public void testInitiatorWithWhiteSpaceInExpression() {
    try {
      identityService.setAuthenticatedUserId("bono");
      runtimeService.startProcessInstanceByKey("InitiatorProcess");
    } finally {
      identityService.setAuthenticatedUserId(null);
    }
    
    assertEquals(1, taskService.createTaskQuery().taskAssignee("bono").count());
  }
  
}
