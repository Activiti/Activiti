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
package org.activiti.engine.test.api.repository;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class ProcessDefinitionSuspensionTest extends PluggableActivitiTestCase {

  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testProcessDefinitionActiveByDefault() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());      
  }
    
  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testSuspendActivateProcessDefinitionById() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    // suspend
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());    
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertTrue(processDefinition.isSuspended());      
    
    // activate
    repositoryService.activateProcessDefinitionById(processDefinition.getId());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());
  }

  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testSuspendActivateProcessDefinitionByKey() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    //suspend
    repositoryService.suspendProcessDefinitionByKey(processDefinition.getKey());    
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertTrue(processDefinition.isSuspended());      
    
    //activate
    repositoryService.activateProcessDefinitionByKey(processDefinition.getKey());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());
  }
  
  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testCannotActivateActiveProcessDefinition() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    try {
      repositoryService.activateProcessDefinitionById(processDefinition.getId());
      fail("Exception exprected");
    }catch (ActivitiException e) {
      // expected
    }
    
  }
  
  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testCannotSuspendActiveProcessDefinition() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    try {
      repositoryService.suspendProcessDefinitionById(processDefinition.getId());
      fail("Exception exprected");
    }catch (ActivitiException e) {
      // expected
    }
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/db/processOne.bpmn20.xml",
          "org/activiti/engine/test/db/processTwo.bpmn20.xml"
          })
  public void testQueryForActiveDefinitions() {    
    
    // default = all definitions
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().list();    
    assertEquals(2, processDefinitionList.size());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());
    
    ProcessDefinition processDefinition = processDefinitionList.get(0);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/db/processOne.bpmn20.xml",
          "org/activiti/engine/test/db/processTwo.bpmn20.xml"
          })
  public void testQueryForSuspendedDefinitions() {    
    
    // default = all definitions
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
      .list();    
    assertEquals(2, processDefinitionList.size());
    
    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());
    
    ProcessDefinition processDefinition = processDefinitionList.get(0);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testStartProcessInstanceForSuspendedProcessDefinition() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    // By id
    try {
      runtimeService.startProcessInstanceById(processDefinition.getId());
      fail("Exception is expected but not thrown");
    } catch(ActivitiException e) {
      assertTextPresentIgnoreCase("cannot start process instance", e.getMessage());
    }
    
    // By Key
    try {
      runtimeService.startProcessInstanceByKey(processDefinition.getKey());
      fail("Exception is expected but not thrown");
    } catch(ActivitiException e) {
      assertTextPresentIgnoreCase("cannot start process instance", e.getMessage());
    }
  }
  

}
