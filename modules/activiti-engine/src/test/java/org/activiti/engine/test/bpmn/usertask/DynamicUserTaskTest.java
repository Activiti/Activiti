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

package org.activiti.engine.test.bpmn.usertask;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.BpmnCacheUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author Tijs Rademakers
 */
public class DynamicUserTaskTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testChangeFormKey() {
    // first test without changing the form key
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dynamicUserTask");
    String processDefinitionId = processInstance.getProcessDefinitionId();
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("test", task.getFormKey());
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    // now test with changing the form key
    ObjectNode infoNode = processEngineConfiguration.getObjectMapper().createObjectNode();
    BpmnCacheUtil.changeFormKey("task1", "test2", infoNode);
    repositoryService.saveProcessDefinitionInfo(processDefinitionId, infoNode);
    
    processInstance = runtimeService.startProcessInstanceByKey("dynamicUserTask");
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("test2", task.getFormKey());
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment
  public void testChangeFormKeyWithExpression() {
    // first test without changing the form key
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("start", "test");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dynamicUserTask", varMap);
    String processDefinitionId = processInstance.getProcessDefinitionId();
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("test", task.getFormKey());
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    // now test with changing the form key
    ObjectNode infoNode = processEngineConfiguration.getObjectMapper().createObjectNode();
    BpmnCacheUtil.changeFormKey("task1", "${anotherKey}", infoNode);
    repositoryService.saveProcessDefinitionInfo(processDefinitionId, infoNode);
    
    varMap = new HashMap<String, Object>();
    varMap.put("anotherKey", "test2");
    processInstance = runtimeService.startProcessInstanceByKey("dynamicUserTask", varMap);
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("test2", task.getFormKey());
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
  }

}
