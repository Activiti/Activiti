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
package org.activiti.cdi.impl.context;

import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.test.CdiActivitiTestCase;
import org.activiti.engine.test.Deployment;

/**
 * 
 * @author Daniel Meyer
 */
public class ThreadContextAssociationTest extends CdiActivitiTestCase {

  @Override
  public void beginConversation() {
    // do not activate conversation
  }
  
  @Override
  public void endConversation() {
    // do not deactivate conversation
  }
  
  @Override
  public void beginRequest() {
   // do not activate
  }
  
  @Override
  public void endRequest() {
    // do not deactivate
  }
  
  @Override
  public void beginSession() {
    // do not activate
  }
  
  @Override
  public void endSession() {
    // do not deactivate
  }
  
  @Deployment
  public void testFallbacktoThreadContext() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    String pid = businessProcess.startProcessByKey("processkey").getId();
    
    assertEquals(pid, businessProcess.getProcessInstanceId());
    
    businessProcess.completeTask();

  }
  
  @Deployment
  public void testBusinessProcessScopedWithJobExecutor() throws InterruptedException {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    businessProcess.startProcessByKey("processkey").getId();
        
    waitForJobExecutorToProcessAllJobs(5000l, 25l);
    
    assertNull(managementService.createJobQuery().singleResult());
    
  }

}
