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
package org.activiti.test.service;

import java.util.List;

import org.activiti.ActivitiException;
import org.activiti.ProcessDefinition;
import org.activiti.ProcessInstance;
import org.activiti.test.ActivitiTestCase;


/**
 * @author Joram Barrez
 */
public class ProcessServiceTest extends ActivitiTestCase {
  
  public void testStartProcessInstanceById() {
    deployProcessResource("org/activiti/test/service/oneTaskProcess.bpmn20.xml");
    List<ProcessDefinition> processDefinitions = processService.findProcessDefinitions();
    assertEquals(1, processDefinitions.size());
    
    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertEquals("oneTaskProcess", processDefinition.getKey());
    assertNotNull(processDefinition.getId());
    
    ProcessInstance processInstance = processService.startProcessInstanceById(processDefinition.getId());
    assertNotNull(processInstance);
    assertEquals(1, processInstance.getActivityNames().size());
    assertEquals("theTask", processInstance.getActivityNames().get(0));
  }
  
  // Test for a bug: when the process engine is rebooted the
  // cache is cleaned and the deployed process definition is
  // removed from the process cache. This led to problems because
  // the id wasnt fetched from the DB after a redeploy.
  
//  TODO figure out how we're going to test this as i don't want access to the process cache in the public api
//  public void testStartProcessInstanceByIdAfterReboot() {
//    deployProcessResource("org/activiti/test/service/oneTaskProcess.bpmn20.xml");
//
//    List<ProcessDefinition> processDefinitions = processService.findProcessDefinitions();
//    assertEquals(1, processDefinitions.size());
//    ProcessInstance processInstance = processService.startProcessInstanceById(processDefinitions.get(0).getId());
//    assertNotNull(processInstance);
//    
//    resetProcessCache();
//    
//    processInstance = processService.startProcessInstanceById(processDefinitions.get(0).getId());
//    assertNotNull(processInstance);
//  }
  
  public void testFindProcessDefinitionById() {
    deployProcessResource("org/activiti/test/service/oneTaskProcess.bpmn20.xml");
    
    List<ProcessDefinition> definitions = processService.findProcessDefinitions();
    assertEquals(1, definitions.size());
    
    ProcessDefinition processDefinition = processService.findProcessDefinitionById(definitions.get(0).getId());
    assertNotNull(processDefinition);
    assertEquals("oneTaskProcess", processDefinition.getKey());
    assertEquals("The One Task Process", processDefinition.getName());
  }
  
  public void testFindProcessDefinitionByNullId() {
    try {
      processService.findProcessDefinitionById(null);
      fail();
    }  catch (ActivitiException e) {
      assertTextPresent("Couldn't find process definiton", e.getMessage());
    }
  }

}
