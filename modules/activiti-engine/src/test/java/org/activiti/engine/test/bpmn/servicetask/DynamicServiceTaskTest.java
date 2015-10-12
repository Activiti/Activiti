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

package org.activiti.engine.test.bpmn.servicetask;

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
public class DynamicServiceTaskTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testChangeClassName() {
    // first test without changing the class name
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("count", 0);
    varMap.put("count2", 0);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dynamicServiceTask", varMap);
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "count"));
    assertEquals(0, runtimeService.getVariable(processInstance.getId(), "count2"));
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    // now test with changing the class name
    varMap = new HashMap<String, Object>();
    varMap.put("count", 0);
    varMap.put("count2", 0);
    processInstance = runtimeService.startProcessInstanceByKey("dynamicServiceTask", varMap);
    
    String processDefinitionId = processInstance.getProcessDefinitionId();
    ObjectNode infoNode = processEngineConfiguration.getObjectMapper().createObjectNode();
    BpmnCacheUtil.changeClassName("service", "org.activiti.engine.test.bpmn.servicetask.DummyServiceTask2", infoNode);
    repositoryService.saveProcessDefinitionInfo(processDefinitionId, infoNode);
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    assertEquals(0, runtimeService.getVariable(processInstance.getId(), "count"));
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "count2"));
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
  }

}
