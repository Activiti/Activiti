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

package org.activiti.examples.bpmn.gateway;

import java.util.List;

import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.TaskQuery;
import org.activiti.test.ActivitiTestCase;


/**
 * @author jbarrez
 */
public class ParallelGatewayTest extends ActivitiTestCase {

  public void testUnbalancedForkJoin() {
//    deployProcessForThisTestMethod();
//    
//    ProcessInstance pi = processService.startProcessInstanceByKey("UnbalancedForkJoin");
//    TaskQuery query = taskService.createTaskQuery()
//                                 .processInstance(pi.getId())
//                                 .orderAsc(TaskQuery.PROPERTY_NAME);
//    List<Task> tasks = query.list(); 
//    assertEquals(3, tasks.size());
//    
//    // Completing the first task should not trigger the join
//    taskService.complete(tasks.get(0).getId());
//    assertEquals(2, query.count());
//    
//    // Completing the second task should trigger the join
//    taskService.complete(tasks.get(1).getId());
//    assertEquals(1, query.count());
//
//    taskService.complete(tasks.get(2).getId());
//
//    // Completing the remaing tasks should trigger the second join and end the process
//    assertProcessInstanceEnded(pi.getId());
  }
  
}
