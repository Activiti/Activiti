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

package org.activiti.engine.test.history;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;


/**
 * @author Frederik Heremans
 */
public class HistoricTaskInstanceUpdateTest extends PluggableActivitiTestCase {

  
  @Deployment
  public void testHistoricTaskInstanceUpdate() {
//    runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest").getId();
//    
//    Task task = taskService.createTaskQuery().singleResult();
//    
//    // Update and save the task's fields before it is finished
//    task.setPriority(12345);
//    task.setDescription("Updated description");
//    task.setName("Updated name");
//    taskService.saveTask(task);   
//
//    taskService.complete(task.getId());
//    assertEquals(1, historyService.createHistoricTaskInstanceQuery().count());
//
//    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
//    assertEquals("Updated description", historicTaskInstance.getDescription());
//    assertEquals("Updated name", historicTaskInstance.getDescription());
  }
}
