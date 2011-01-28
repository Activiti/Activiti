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
package org.activiti.migration.test.process.conversion;

import org.activiti.engine.task.Task;
import org.activiti.migration.test.MigrationTestCase;


/**
 * @author Joram Barrez
 */
public class TaskNodeConversionTest extends MigrationTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    deployJbpmProcess("org/activiti/migration/test/process/conversion/tasknode/actor-id");
    deployJbpmProcess("org/activiti/migration/test/process/conversion/tasknode/pooledactors");
  }
  
  public void testActorIdToAssigneeConversion() {
    String migratedBpmn20Xml = getConvertedProcess("actorIdProcess-1");
    String deployId = repositoryService.createDeployment()
      .addString("actorIdProcess.bpmn20.xml", migratedBpmn20Xml) .deploy().getId();
    
    String procId = runtimeService.startProcessInstanceByKey("actorIdProcess").getId();
    Task task = taskService.createTaskQuery().processInstanceId(procId).taskAssignee("kermit").singleResult();
    assertEquals("myTask", task.getName());
    
    taskService.complete(task.getId());
    assertProcessEnded(procId);
    
    repositoryService.deleteDeployment(deployId, true);
  }
  
  public void testPooledActorsConversion() {
    String migratedBpmn20Xml = getConvertedProcess("pooledActorsProcess-1");
    String deployId = repositoryService.createDeployment()
      .addString("pooledActorsProcess.bpmn20.xml", migratedBpmn20Xml).deploy().getId();
    
    String procId = runtimeService.startProcessInstanceByKey("pooledActorsProcess").getId();
    assertNotNull(taskService.createTaskQuery().processInstanceId(procId).taskCandidateUser("kermit").singleResult());
    assertNotNull(taskService.createTaskQuery().processInstanceId(procId).taskCandidateUser("gonzo").singleResult());
    assertNotNull(taskService.createTaskQuery().processInstanceId(procId).taskCandidateUser("fozzie").singleResult());
    assertNull(taskService.createTaskQuery().processInstanceId(procId).taskCandidateUser("missPiggy").singleResult());
    
    Task task = taskService.createTaskQuery().processInstanceId(procId).singleResult();
    assertEquals("myTask", task.getName());
    assertNull(task.getAssignee());
    
    taskService.complete(task.getId());
    assertProcessEnded(procId);
    
    repositoryService.deleteDeployment(deployId, true);
  
  }
  
}
