package org.activiti.rest.api.task;

import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class TaskOperationResourceTest extends BaseRestTestCase {

  @Deployment
  public void testClaimTask() throws Exception {
    runtimeService.startProcessInstanceByKey("simpleProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("WaitTask", task.getName());
    assertNull(task.getAssignee());
    
    ClientResource client = getAuthenticatedClient("task/" + task.getId() + "/claim");
    Representation response = client.put(null);
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.get("success").getBooleanValue());
    
    task = taskService.createTaskQuery().singleResult();
    assertEquals("kermit", task.getAssignee());
    
    taskService.complete(task.getId());
    
    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertEquals(0, instanceList.size());
  }
  
  @Deployment
  public void testCompleteTask() throws Exception {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("simpleProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("WaitTask", task.getName());
    
    ClientResource client = getAuthenticatedClient("task/" + task.getId() + "/complete");
    ObjectNode varNode = objectMapper.createObjectNode();
    varNode.put("myVar", "test");
    Representation response = client.put(varNode);
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.get("success").getBooleanValue());
    
    task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("AnotherTask", task.getName());
    
    Object varValue = runtimeService.getVariable(instance.getId(), "myVar");
    assertNotNull(varValue);
    assertEquals("test", varValue);
    
    taskService.complete(task.getId());
    
    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertEquals(0, instanceList.size());
  }
}
