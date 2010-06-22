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
import org.activiti.test.ActivitiTestCase;
import org.activiti.test.ProcessDeclared;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jbarrez
 */
public class ParallelGatewayTest extends ActivitiTestCase {

  @Test
  @ProcessDeclared
  @Ignore
  public void testUnbalancedForkJoin() {

    ProcessInstance processInstance = processEngineBuilder.getProcessService().startProcessInstanceByKey("UnbalancedForkJoin");

    // Completing the remaining tasks should trigger the second join and end the
    // process
    processEngineBuilder.expectProcessEnds(processInstance.getId());

    TaskQuery query = processEngineBuilder.getTaskService().createTaskQuery().processInstance(processInstance.getId()).orderAsc(TaskQuery.PROPERTY_NAME);
    List<Task> tasks = query.list();
    assertEquals(3, tasks.size());

    // Completing the first task should not trigger the join
    processEngineBuilder.getTaskService().complete(tasks.get(0).getId());
    assertEquals(2, query.count());

    // Completing the second task should trigger the join
    processEngineBuilder.getTaskService().complete(tasks.get(1).getId());
    assertEquals(1, query.count());

    processEngineBuilder.getTaskService().complete(tasks.get(2).getId());
  }

}
