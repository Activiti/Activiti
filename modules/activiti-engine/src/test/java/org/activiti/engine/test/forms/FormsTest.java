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

package org.activiti.engine.test.forms;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.form.StartForm;
import org.activiti.engine.form.TaskForm;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Tom Baeyens
 */
public class FormsTest extends ActivitiInternalTestCase {

  @Deployment(resources = { 
    "org/activiti/engine/test/forms/FormsProcess.bpmn20.xml", 
    "org/activiti/engine/test/forms/start.form", 
    "org/activiti/engine/test/forms/task.form" })
  public void testTaskFormsWithVacationRequestProcess() {
    StartForm startForm = formService.getStartForm("FormsProcess:1");
    assertNotNull(startForm);
    assertEquals(deploymentId, startForm.getDeploymentId());
    assertEquals("org/activiti/engine/test/forms/start.form", startForm.getFormKey());
    assertEquals(new HashMap<String, Object>(), startForm.getProperties());
    assertEquals("FormsProcess:1", startForm.getProcessDefinition().getId());
    
    Object renderedStartForm = formService.getRenderedStartForm("FormsProcess:1");
    assertEquals("start form content", renderedStartForm);
    
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("room", "5b");
    properties.put("speaker", "Mike");
    String processInstanceId = formService.submitStartForm("FormsProcess:1", properties).getId();
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("room", "5b");
    expectedVariables.put("speaker", "Mike");
    
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
    assertEquals(expectedVariables, variables);
    
    Task task = taskService.createTaskQuery().singleResult();
    String taskId = task.getId();
    TaskForm taskForm = formService.getTaskForm(taskId);
    assertEquals(deploymentId, taskForm.getDeploymentId());
    assertEquals("org/activiti/engine/test/forms/task.form", taskForm.getFormKey());
    assertEquals(new HashMap<String, Object>(), taskForm.getProperties());
    assertEquals(taskId, taskForm.getTask().getId());
    
    assertEquals("Mike is speaking in room 5b", formService.getRenderedTaskForm(taskId));
  }
}
