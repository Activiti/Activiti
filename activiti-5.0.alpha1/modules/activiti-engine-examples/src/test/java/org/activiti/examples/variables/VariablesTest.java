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
package org.activiti.examples.variables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.test.ActivitiTestCase;


/**
 * @author Tom Baeyens
 */
public class VariablesTest extends ActivitiTestCase {

  public void testTaskVariableAccess() {
    deployProcessForThisTestMethod();
    
    // Start process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("cost center", 928374L);
    variables.put("customer", "coca-cola");
    variables.put("message", "<xml />");
    ProcessInstance processInstance = processService.startProcessInstanceByKey("taskAssigneeProcess", variables);
    
//    String processInstanceId = processInstance.getId();
//    processService.findProcessInstanceById(processInstanceId);
//
//    // Get task list
//    List<Task> tasks = taskService.findAssignedTasks("kermit");
//    assertEquals(1, tasks.size());
//    Task myTask = tasks.get(0);
//    assertEquals("Schedule meeting", myTask.getName());
//    assertEquals("Schedule an engineering meeting for next week with the new hire.", myTask.getDescription());
//
//    // Complete task. Process is now finished
//    taskService.complete(myTask.getId());
//   
//    // assert if the process instance completed
//    assertProcessInstanceEnded(processInstanceId);

  }
}
