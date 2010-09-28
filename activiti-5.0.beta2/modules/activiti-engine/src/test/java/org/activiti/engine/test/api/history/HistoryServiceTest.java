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

package org.activiti.engine.test.api.history;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Frederik Heremans
 */
public class HistoryServiceTest extends ActivitiInternalTestCase {
  
  public void testFindHistoricProcessInstanceByUnexistingId() {
    HistoricProcessInstance historicProcessInstance = historyService.findHistoricProcessInstanceById("unexisting");
    assertNull(historicProcessInstance);
  }
  
  public void testFindHistoricProcessInstanceNullArgument() {
    try {
      historyService.findHistoricProcessInstanceById(null);
      fail("ActivitiException expected");
    } catch(ActivitiException ae) {
      assertTextPresent("processInstanceId is null", ae.getMessage());
    }
  }
  
  
  @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceQuery() {
    // With a clean ProcessEngine, no instances should be available
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 0);
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 1);
    
    // Complete the task and check if the size is count 1
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 1);
        
  }
}
