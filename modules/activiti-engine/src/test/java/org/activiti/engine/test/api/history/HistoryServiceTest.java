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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 * @author Frederik Heremans
 * @author Falko Menge
 */
public class HistoryServiceTest extends PluggableActivitiTestCase {
  
  @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceQuery() {
    // With a clean ProcessEngine, no instances should be available
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 0);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 1);
    
    // Complete the task and check if the size is count 1
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 1);
        
  }

  @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceUserIdAndActivityId() {
    identityService.setAuthenticatedUserId("johndoe");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertEquals("johndoe", historicProcessInstance.getStartUserId());
    assertEquals("theStart", historicProcessInstance.getStartActivityId());
    
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());
    
    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertEquals("theEnd", historicProcessInstance.getEndActivityId());
  }
  
  @Deployment(resources={
    "org/activiti/examples/bpmn/callactivity/orderProcess.bpmn20.xml",
    "org/activiti/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml"       
  })
  public void testOrderProcessWithCallActivity() {
    // After the process has started, the 'verify credit history' task should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();
    
    // Completing the task with approval, will end the subprocess and continue the original process
    taskService.complete(verifyCreditTask.getId(), CollectionUtil.singletonMap("creditApproved", true));
    Task prepareAndShipTask = taskQuery.singleResult();
    assertEquals("Prepare and Ship", prepareAndShipTask.getName());
    
     //verify
    HistoricProcessInstance historicProcessInstance = 
    	  historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertNotNull(historicProcessInstance);
    assertTrue(historicProcessInstance.getProcessDefinitionId().contains("checkCreditProcess"));
  }
  
  @Deployment(resources = {
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml",
    "org/activiti/examples/bpmn/callactivity/orderProcess.bpmn20.xml",
    "org/activiti/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml"       
  })
  public void testHistoricProcessInstanceQueryByProcessDefinitionKey() {
    
    String processDefinitionKey = "oneTaskProcess";
    runtimeService.startProcessInstanceByKey(processDefinitionKey);
    runtimeService.startProcessInstanceByKey("orderProcess");
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(processDefinitionKey).singleResult();
    assertNotNull(historicProcessInstance);
    assertTrue(historicProcessInstance.getProcessDefinitionId().startsWith(processDefinitionKey));
    assertEquals("theStart", historicProcessInstance.getStartActivityId());

    // now complete the task to end the process instance
    Task task = taskService.createTaskQuery().processDefinitionKey("checkCreditProcess").singleResult();
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("creditApproved", true);
    taskService.complete(task.getId(), map);
    
    // and make sure the super process instance is set correctly on the HistoricProcessInstance    
    HistoricProcessInstance historicProcessInstanceSub = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("checkCreditProcess").singleResult();
    HistoricProcessInstance historicProcessInstanceSuper = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("orderProcess").singleResult();    
    assertEquals(historicProcessInstanceSuper.getId(), ((HistoricProcessInstanceEntity)historicProcessInstanceSub).getSuperProcessInstanceId());    
  }

  @Deployment(resources = {
          "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml",
          "org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml"       
        })
  public void testHistoricProcessInstanceQueryByProcessInstanceIds() {
    HashSet<String> processInstanceIds = new HashSet<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess", i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess2", "1").getId());
    
    // start an instance that will not be part of the query
    runtimeService.startProcessInstanceByKey("oneTaskProcess2", "2");
            
    HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery().processInstanceIds(processInstanceIds);
    assertEquals(5, processInstanceQuery.count());
    
    List<HistoricProcessInstance> processInstances = processInstanceQuery.list();
    assertNotNull(processInstances);
    assertEquals(5, processInstances.size());
    
    for (HistoricProcessInstance historicProcessInstance : processInstances) {
      assertTrue(processInstanceIds.contains(historicProcessInstance.getId()));
    }
  }

  public void testHistoricProcessInstanceQueryByProcessInstanceIdsEmpty() {
    try {
      historyService.createHistoricProcessInstanceQuery().processInstanceIds(new HashSet<String>());
      fail("ActivitiException expected");
    } catch (ActivitiException re) {
      assertTextPresent("Set of process instance ids is empty", re.getMessage());
    }
  }

  public void testHistoricProcessInstanceQueryByProcessInstanceIdsNull() {
    try {
      historyService.createHistoricProcessInstanceQuery().processInstanceIds(null);
      fail("ActivitiException expected");
    } catch (ActivitiException re) {
      assertTextPresent("Set of process instance ids is null", re.getMessage());
    }
  }
}
