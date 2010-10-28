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

package org.activiti.spring.test.jpa;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.test.PvmTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Test using spring-orm in spring-bean combined with JPA-variables in activiti.
 * 
 * @author Frederik Heremans
 */
public class JPASpringTest extends PvmTestCase{

  public void testJPAVariableSpring() {
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("org/activiti/spring/test/jpa/JPASpringTest-context.xml");
    
    RuntimeService runtimeService = (RuntimeService) applicationContext.getBean("runtimeService");
    TaskService taskService = (TaskService) applicationContext.getBean("taskService"); 
    RepositoryService repositoryService = (RepositoryService) applicationContext.getBean("repositoryService"); 
    
    // Simulate form submission with customerName and amount
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("customerName", "John Doe");
    variables.put("amount", 15000L);
    
    // ----------------------------
    // First process instance that follows the path of an approved loan request
    // ----------------------------
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("LoanRequestProcess", variables);
    
    // Variable should be present containing the loanRequest created by the spring bean
    Object value = runtimeService.getVariable(processInstance.getId(), "loanRequest");
    assertNotNull(value);
    assertTrue(value instanceof LoanRequest);
    LoanRequest request = (LoanRequest) value;
    assertEquals("John Doe", request.getCustomerName());
    assertEquals(15000L, request.getAmount().longValue());
    assertFalse(request.isApproved());
    
    // We will approve the request, which will update the entity
    variables = new HashMap<String, Object>();
    variables.put("approvedByManager", Boolean.TRUE);
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId(), variables);
    
    // If approved, the processsInstance should be finished, gateway based on loanRequest.approved value
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    
    // ----------------------------
    // Second process instance that follows the path of an disapproved loan request
    // ----------------------------
    variables = new HashMap<String, Object>();
    variables.put("customerName", "Jane Doe");
    variables.put("amount", 50000L);
    
    processInstance = runtimeService.startProcessInstanceByKey("LoanRequestProcess", variables);
    
    // Variable should be present containing the loanRequest created by the spring bean
    value = runtimeService.getVariable(processInstance.getId(), "loanRequest");
    assertNotNull(value);
    assertTrue(value instanceof LoanRequest);
    request = (LoanRequest) value;
    assertEquals("Jane Doe", request.getCustomerName());
    assertEquals(50000L, request.getAmount().longValue());
    assertFalse(request.isApproved());
    
    // We will disapprove the request, which will update the entity
    variables = new HashMap<String, Object>();
    variables.put("approvedByManager", Boolean.FALSE);
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId(), variables);
    
    runtimeService.getVariable(processInstance.getId(), "loanRequest");
    request = (LoanRequest) value;
    assertFalse(request.isApproved());
    
    // If disapproved, an extra task will be available instead of the process ending
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    
    assertEquals("Send rejection letter", task.getName());
    
    // Finally cleanup the deployment
    String deploymentId = repositoryService.createProcessDefinitionQuery().processDefinitionKey("LoanRequestProcess").singleResult().getDeploymentId();
    repositoryService.deleteDeploymentCascade(deploymentId);
  }
  
  
}
