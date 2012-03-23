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
package org.activiti.cdi.test.api.annotation;

import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.test.CdiActivitiTestCase;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * @author Daniel Meyer
 */
public class TaskIdTest extends CdiActivitiTestCase {

  @Test
  @Deployment
  public void testTaskIdInjectable() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    businessProcess.startProcessByKey("keyOfTheProcess");  
    
    businessProcess.startTask(taskService.createTaskQuery().singleResult().getId());   
    
    // assert that now the 'taskId'-bean can be looked up
    assertNotNull(getBeanInstance("taskId"));
    
    businessProcess.completeTask();
  }
  
}
