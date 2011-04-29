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
public class ContextScopingTest extends CdiActivitiTestCase {
  
  @Override
  protected void activateConversationContext() {
   // do nothing -> deacivated
  }
  
  @Deployment
  public void testFallbackToRequestContext() {    
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    
    beginRequest();
    
    String pid = businessProcess.startProcessByKey("processkey").getId();    
    assertEquals(pid, businessProcess.getProcessInstanceId());
    
    endRequest();
    beginRequest();
    
    // assert that now the process is not associated with the new request.
    assertNull(businessProcess.getProcessInstanceId());
    businessProcess.resumeProcessById(pid);
    businessProcess.completeTask();
    
    endRequest();    
  }
  
}
