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
package org.activiti.cdi.test.impl.context;

import org.activiti.cdi.test.CdiActivitiTestCase;
import org.activiti.cdi.test.impl.beans.ProcessScopedMessageBean;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * @author Daniel Meyer
 */
public class ThreadContextAssociationTest extends CdiActivitiTestCase {
  
  @Test
  @Deployment
  public void testBusinessProcessScopedWithJobExecutor() throws InterruptedException {
    String pid = runtimeService.startProcessInstanceByKey("processkey").getId();
        
    waitForJobExecutorToProcessAllJobs(5000l, 25l);
        
    assertNull(managementService.createJobQuery().singleResult());
    
    ProcessScopedMessageBean messageBean = (ProcessScopedMessageBean) runtimeService.getVariable(pid, "processScopedMessageBean");
    assertEquals("test", messageBean.getMessage());
    
    runtimeService.signal(pid);
    
  }

}
