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

package org.activiti.rest.service.api.runtime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Test for all REST-operations related to the Task collection resource.
 * 
 * @author Frederik Heremans
 */
public class TaskQueryResourceTest extends BaseSpringRestTestCase {
 
  /**
   * Test querying tasks.
   * GET runtime/tasks
   */
  @Deployment
  public void testQueryTasks() throws Exception {
    try {
      Calendar adhocTaskCreate = Calendar.getInstance();
      adhocTaskCreate.set(Calendar.MILLISECOND, 0);
      
      Calendar processTaskCreate = Calendar.getInstance();
      processTaskCreate.add(Calendar.HOUR, 2);
      processTaskCreate.set(Calendar.MILLISECOND, 0);
      
      Calendar inBetweenTaskCreation = Calendar.getInstance();
      inBetweenTaskCreation.add(Calendar.HOUR, 1);
      
      processEngineConfiguration.getClock().setCurrentTime(adhocTaskCreate.getTime());
      Task adhocTask = taskService.newTask();
      adhocTask.setAssignee("gonzo");
      adhocTask.setOwner("owner");
      adhocTask.setDelegationState(DelegationState.PENDING);
      adhocTask.setDescription("Description one");
      adhocTask.setName("Name one");
      adhocTask.setDueDate(adhocTaskCreate.getTime());
      adhocTask.setPriority(100);
      adhocTask.setFormKey("myForm.json");
      adhocTask.setCategory("some-category");
      taskService.saveTask(adhocTask);
      taskService.addUserIdentityLink(adhocTask.getId(), "misspiggy", IdentityLinkType.PARTICIPANT);

      processEngineConfiguration.getClock().setCurrentTime(processTaskCreate.getTime());
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "myBusinessKey");
      Task processTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      processTask.setParentTaskId(adhocTask.getId());
      processTask.setPriority(50);
      processTask.setDueDate(processTaskCreate.getTime());
      taskService.saveTask(processTask);
      
      // Check filter-less to fetch all tasks
      String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_QUERY);
      ObjectNode requestNode = objectMapper.createObjectNode();
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId(), adhocTask.getId());
      
      // Name filtering
      requestNode.removeAll();
      requestNode.put("name", "Name one");
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Name like filtering
      requestNode.removeAll();
      requestNode.put("nameLike", "%one");
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());

      // Description filtering
      requestNode.removeAll();
      requestNode.put("description", "Description one");
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Description like filtering
      requestNode.removeAll();
      requestNode.put("descriptionLike", "%one");
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Priority filtering
      requestNode.removeAll();
      requestNode.put("priority", 100);
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Mininmum Priority filtering
      requestNode.removeAll();
      requestNode.put("minimumPriority", 70);
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Maximum Priority filtering
      requestNode.removeAll();
      requestNode.put("maximumPriority", 70);
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Owner filtering
      requestNode.removeAll();
      requestNode.put("owner", "owner");
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Assignee filtering
      requestNode.removeAll();
      requestNode.put("assignee", "gonzo");
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Owner like filtering
      requestNode.removeAll();
      requestNode.put("ownerLike", "owne%");
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Assignee like filtering
      requestNode.removeAll();
      requestNode.put("assigneeLike", "%onzo");
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Unassigned filtering
      requestNode.removeAll();
      requestNode.put("unassigned", true);
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Delegation state filtering
      requestNode.removeAll();
      requestNode.put("delegationState", "pending");
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Candidate user filtering
      requestNode.removeAll();
      requestNode.put("candidateUser", "kermit");
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Candidate group filtering
      requestNode.removeAll();
      requestNode.put("candidateGroup", "sales");
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());

      // Candidate group In filtering
      requestNode.removeAll();
      ArrayNode arrayNode =  requestNode.arrayNode();
      
      arrayNode.add("sales");
      arrayNode.add("someOtherGroup");
      
      requestNode.put("candidateGroupIn", arrayNode);
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());

      // Involved user filtering
      requestNode.removeAll();
      requestNode.put("involvedUser", "misspiggy");
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Process instance filtering
      requestNode.removeAll();
      requestNode.put("processInstanceId", processInstance.getId());
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Process instance id in filtering
      requestNode.removeAll();

      arrayNode =  requestNode.arrayNode();
      arrayNode.add(processInstance.getId());

      requestNode.put("processInstanceIdIn", arrayNode);
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Execution filtering
      requestNode.removeAll();
      requestNode.put("executionId", processInstance.getId());
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Process instance businesskey filtering
      requestNode.removeAll();
      requestNode.put("processInstanceBusinessKey", "myBusinessKey");
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Process instance businesskey like filtering
      requestNode.removeAll();
      requestNode.put("processInstanceBusinessKeyLike", "myBusiness%");
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Process definition key
      requestNode.removeAll();
      requestNode.put("processDefinitionKey", "oneTaskProcess");
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Process definition key like
      requestNode.removeAll();
      requestNode.put("processDefinitionKeyLike", "%TaskProcess");
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Process definition name
      requestNode.removeAll();
      requestNode.put("processDefinitionName", "The One Task Process");
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Process definition name like
      requestNode.removeAll();
      requestNode.put("processDefinitionNameLike", "The One %");
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // CeatedOn filtering
      requestNode.removeAll();
      requestNode.put("createdOn", getISODateString(adhocTaskCreate.getTime()));
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // CreatedAfter filtering
      requestNode.removeAll();
      requestNode.put("createdAfter", getISODateString(inBetweenTaskCreation.getTime()));
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // CreatedBefore filtering
      requestNode.removeAll();
      requestNode.put("createdBefore", getISODateString(inBetweenTaskCreation.getTime()));
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Subtask exclusion
      requestNode.removeAll();
      requestNode.put("excludeSubTasks", true);
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Task definition key filtering
      requestNode.removeAll();
      requestNode.put("taskDefinitionKey", "processTask");
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Task definition key like filtering
      requestNode.removeAll();
      requestNode.put("taskDefinitionKeyLike", "process%");
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Duedate filtering
      requestNode.removeAll();
      requestNode.put("dueDate", getISODateString(adhocTaskCreate.getTime()));
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Due after filtering
      requestNode.removeAll();
      requestNode.put("dueAfter", getISODateString(inBetweenTaskCreation.getTime()));
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Due before filtering
      requestNode.removeAll();
      requestNode.put("dueBefore", getISODateString(inBetweenTaskCreation.getTime()));
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Suspend process-instance to have a supended task
      runtimeService.suspendProcessInstanceById(processInstance.getId());
      
      // Suspended filering
      requestNode.removeAll();
      requestNode.put("active", false);
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
      // Active filtering
      requestNode.removeAll();
      requestNode.put("active", true);
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());
      
      // Filtering by category
      requestNode.removeAll();
      requestNode.put("category", "some-category");
      assertResultsPresentInPostDataResponse(url, requestNode, adhocTask.getId());

      // Filtering without duedate
      requestNode.removeAll();
      requestNode.put("withoutDueDate", true);
      // No response should be returned, no tasks without a duedate yet 
      assertResultsPresentInPostDataResponse(url, requestNode);
      
      processTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      processTask.setDueDate(null);
      taskService.saveTask(processTask);
      assertResultsPresentInPostDataResponse(url, requestNode, processTask.getId());
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        if(task.getExecutionId() == null) {
          taskService.deleteTask(task.getId(), true);
        }
      }
    }
  }
  
  /**
   * Test querying tasks using task and process variables. GET runtime/tasks
   */
  @Deployment
  public void testQueryTasksWithVariables() throws Exception {
    HashMap<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("stringVar", "Azerty");
    processVariables.put("intVar", 67890);
    processVariables.put("booleanVar", false);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", processVariables);
    Task processTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "Abcdef");
    variables.put("intVar", 12345);
    variables.put("booleanVar", true);
    taskService.setVariablesLocal(processTask.getId(), variables);
    
    // Additional tasks to confirm it's filtered out
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    ObjectNode taskVariableRequestNode = objectMapper.createObjectNode();
    ArrayNode variableArray = objectMapper.createArrayNode();
    ObjectNode variableNode = objectMapper.createObjectNode();
    variableArray.add(variableNode);
    taskVariableRequestNode.put("taskVariables", variableArray);

    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_QUERY);
    
    // String equals
    variableNode.put("name", "stringVar");
    variableNode.put("value", "Abcdef");
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());

    // Integer equals
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 12345);
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    
    // Boolean equals
    variableNode.removeAll();
    variableNode.put("name", "booleanVar");
    variableNode.put("value", true);
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    
    // String not equals
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "ghijkl");
    variableNode.put("operation", "notEquals");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());

    // Integer not equals
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 45678);
    variableNode.put("operation", "notEquals");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    
    // Boolean not equals
    variableNode.removeAll();
    variableNode.put("name", "booleanVar");
    variableNode.put("value", false);
    variableNode.put("operation", "notEquals");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    
    // String equals ignore case
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "abCDEF");
    variableNode.put("operation", "equalsIgnoreCase");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    
    // String not equals ignore case
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "HIJKLm");
    variableNode.put("operation", "notEqualsIgnoreCase");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    
    // String equals without value
    variableNode.removeAll();
    variableNode.put("value", "Abcdef");
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    
    // Greater than
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 12300);
    variableNode.put("operation", "greaterThan");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    variableNode.put("value", 12345);
    variableNode.put("operation", "greaterThan");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode);
    
    // Greater than or equal
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 12300);
    variableNode.put("operation", "greaterThanOrEquals");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    variableNode.put("value", 12345);
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    
    // Less than
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 12400);
    variableNode.put("operation", "lessThan");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    variableNode.put("value", 12345);
    variableNode.put("operation", "lessThan");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode);
    
    // Less than or equal
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 12400);
    variableNode.put("operation", "lessThanOrEquals");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    variableNode.put("value", 12345);
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());
    
    // Like
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "Abcde%");
    variableNode.put("operation", "like");
    
    // Any other operation but equals without value
    variableNode.removeAll();
    variableNode.put("value", "abcdef");
    variableNode.put("operation", "notEquals");
    
    assertResultsPresentInPostDataResponseWithStatusCheck(url, taskVariableRequestNode, HttpStatus.SC_BAD_REQUEST);
    
    // Illegal (but existing) operation
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "abcdef");
    variableNode.put("operation", "operationX");
    
    assertResultsPresentInPostDataResponseWithStatusCheck(url, taskVariableRequestNode, HttpStatus.SC_BAD_REQUEST);

    // String like case does not match
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "%Abc%");
    variableNode.put("operation", "like");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());

    // String like case does not match
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "%Bcde%");
    variableNode.put("operation", "like");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode);

    // String like ignore case
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "%Bcde%");
    variableNode.put("operation", "likeIgnoreCase");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode, processTask.getId());

    // String like ignore case process not found
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "%xyz%");
    variableNode.put("operation", "likeIgnoreCase");
    assertResultsPresentInPostDataResponse(url, taskVariableRequestNode);

    // Process variables
    ObjectNode processVariableRequestNode = objectMapper.createObjectNode();
    variableArray = objectMapper.createArrayNode();
    variableNode = objectMapper.createObjectNode();
    variableArray.add(variableNode);
    processVariableRequestNode.put("processInstanceVariables", variableArray);
    
    // String equals
    variableNode.put("name", "stringVar");
    variableNode.put("value", "Azerty");
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());

    // Integer equals
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 67890);
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    
    // Boolean equals
    variableNode.removeAll();
    variableNode.put("name", "booleanVar");
    variableNode.put("value", false);
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    
    // String not equals
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "ghijkl");
    variableNode.put("operation", "notEquals");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());

    // Integer not equals
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 45678);
    variableNode.put("operation", "notEquals");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    
    // Boolean not equals
    variableNode.removeAll();
    variableNode.put("name", "booleanVar");
    variableNode.put("value", true);
    variableNode.put("operation", "notEquals");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    
    // String equals ignore case
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "azeRTY");
    variableNode.put("operation", "equalsIgnoreCase");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    
    // String not equals ignore case
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "HIJKLm");
    variableNode.put("operation", "notEqualsIgnoreCase");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    
    // String equals without value
    variableNode.removeAll();
    variableNode.put("value", "Azerty");
    variableNode.put("operation", "equals");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    
    // Greater than
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 67800);
    variableNode.put("operation", "greaterThan");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    variableNode.put("value", 67890);
    variableNode.put("operation", "greaterThan");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode);
    
    // Greater than or equal
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 67800);
    variableNode.put("operation", "greaterThanOrEquals");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    variableNode.put("value", 67890);
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    
    // Less than
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 67900);
    variableNode.put("operation", "lessThan");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    variableNode.put("value", 67890);
    variableNode.put("operation", "lessThan");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode);
    
    // Less than or equal
    variableNode.removeAll();
    variableNode.put("name", "intVar");
    variableNode.put("value", 67900);
    variableNode.put("operation", "lessThanOrEquals");
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    variableNode.put("value", 67890);
    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
    
    // Like
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "Azert%");
    variableNode.put("operation", "like");

    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());

    // incomplete Like missing wildcard does not match
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "Azert");
    variableNode.put("operation", "like");

    assertResultsPresentInPostDataResponse(url, processVariableRequestNode);

    // complete Like missing wildcard does match
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "Azerty");
    variableNode.put("operation", "like");

    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());

    // Like ignore case
    variableNode.removeAll();
    variableNode.put("name", "stringVar");
    variableNode.put("value", "%aZeRt%");
    variableNode.put("operation", "likeIgnoreCase");

    assertResultsPresentInPostDataResponse(url, processVariableRequestNode, processTask.getId());
  }
  
  /**
  * Test querying tasks.
  * GET runtime/tasks
  */
  public void testQueryTasksWithPaging() throws Exception {
    try {
      Calendar adhocTaskCreate = Calendar.getInstance();
      adhocTaskCreate.set(Calendar.MILLISECOND, 0);

      processEngineConfiguration.getClock().setCurrentTime(adhocTaskCreate.getTime());
      List<String> taskIdList = new ArrayList<String>();
      for (int i = 0; i < 10; i++) {
        Task adhocTask = taskService.newTask();
        adhocTask.setAssignee("gonzo");
        adhocTask.setOwner("owner");
        adhocTask.setDelegationState(DelegationState.PENDING);
        adhocTask.setDescription("Description one");
        adhocTask.setName("Name one");
        adhocTask.setDueDate(adhocTaskCreate.getTime());
        adhocTask.setPriority(100);
        taskService.saveTask(adhocTask);
        taskService.addUserIdentityLink(adhocTask.getId(), "misspiggy", IdentityLinkType.PARTICIPANT);
        taskIdList.add(adhocTask.getId());
      }
      Collections.sort(taskIdList);
       
      // Check filter-less to fetch all tasks
      String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_TASK_QUERY);
      ObjectNode requestNode = objectMapper.createObjectNode();
      String[] taskIds = new String[] {taskIdList.get(0), taskIdList.get(1), taskIdList.get(2)};
      assertResultsPresentInPostDataResponse(url + "?size=3&sort=id&order=asc", requestNode, taskIds);
      
      taskIds = new String[] {taskIdList.get(4), taskIdList.get(5), taskIdList.get(6), taskIdList.get(7)};
      assertResultsPresentInPostDataResponse(url + "?start=4&size=4&sort=id&order=asc", requestNode, taskIds);
      
      taskIds = new String[] {taskIdList.get(8), taskIdList.get(9)};
      assertResultsPresentInPostDataResponse(url + "?start=8&size=10&sort=id&order=asc", requestNode, taskIds);
      
    } finally {
      // Clean adhoc-tasks even if test fails
      List<Task> tasks = taskService.createTaskQuery().list();
      for(Task task : tasks) {
        if(task.getExecutionId() == null) {
          taskService.deleteTask(task.getId(), true);
        }
      }
    }
  }
}
