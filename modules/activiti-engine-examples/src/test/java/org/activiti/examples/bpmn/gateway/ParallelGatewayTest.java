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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.TaskQuery;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class ParallelGatewayTest {
  
  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  @ProcessDeclared
  @Ignore
  public void testUnbalancedForkJoin() {
    
    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("UnbalancedForkJoin");
    TaskQuery query = deployer.getTaskService().createTaskQuery()
                                 .processInstance(pi.getId())
                                 .orderAsc(TaskQuery.PROPERTY_NAME);
    List<Task> tasks = query.list(); 
    assertEquals(3, tasks.size());
    
    // Completing the first task should not trigger the join
    deployer.getTaskService().complete(tasks.get(0).getId());
    assertEquals(2, query.count());
    
    // Completing the second task should trigger the join
    deployer.getTaskService().complete(tasks.get(1).getId());
    tasks = query.list();
    assertEquals(2, tasks.size());
    assertEquals("Task 4", tasks.get(1).getName());
    
    // Completing the remaing tasks should trigger the second join and end the process
    deployer.getTaskService().complete(tasks.get(0).getId());
    deployer.getTaskService().complete(tasks.get(1).getId());
    deployer.expectProcessEnds(pi.getId());

    deployer.expectProcessEnds(pi.getId());
  }
  
}
