package org.activiti.rest.service.api.legacy;

import java.util.List;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class StartProcessInstanceResourceTest extends BaseRestTestCase {

  @Deployment
  public void testStartInstance() throws Exception {
    ClientResource client = getAuthenticatedClient("process-instance");
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKey", "simpleProcess");
    Representation response = client.post(requestNode);
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    
    String processInstanceId = responseNode.get("processInstanceId").asText();
    assertNotNull(processInstanceId);
    
    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertEquals(1, instanceList.size());
    assertEquals(processInstanceId, instanceList.get(0).getProcessInstanceId());
    
    Task task = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
    assertNotNull(task);
    assertEquals("WaitTask", task.getName());
    
    taskService.complete(task.getId());
    
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertEquals(0, instanceList.size());
  }
  
  public void testStartInstanceUnexistingKey() throws Exception {
    ClientResource client = getAuthenticatedClient("process-instance");
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKey", "unexistingProcess");
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch(ResourceException re) {
      // Check status
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND.getCode(), re.getStatus().getCode());
    }
  }
  
  @Deployment
  public void testStartInstanceWithVariables() throws Exception {
    ClientResource client = getAuthenticatedClient("process-instance");
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("processDefinitionKey", "simpleProcess");
    requestNode.put("var1", "test");
    requestNode.put("var2", 1);
    Representation response = client.post(requestNode);
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    
    String processInstanceId = responseNode.get("processInstanceId").asText();
    assertNotNull(processInstanceId);
    
    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertEquals(1, instanceList.size());
    assertEquals(processInstanceId, instanceList.get(0).getProcessInstanceId());
    
    Map<String, Object> variableMap = runtimeService.getVariables(processInstanceId);
    assertTrue(variableMap.containsKey("var1"));
    assertEquals("test", variableMap.get("var1"));
    assertTrue(variableMap.containsKey("var2"));
    assertEquals(1, variableMap.get("var2"));
    
    Task task = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
    assertNotNull(task);
    assertEquals("WaitTask", task.getName());
    
    taskService.complete(task.getId());
    
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertEquals(0, instanceList.size());
  }
}
