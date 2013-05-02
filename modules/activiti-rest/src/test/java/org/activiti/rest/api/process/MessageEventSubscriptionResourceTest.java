package org.activiti.rest.api.process;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.util.List;
import java.util.Map;

public class MessageEventSubscriptionResourceTest extends BaseRestTestCase {

  @Deployment
  public void testMessageEvent() throws Exception {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("messageEventProcess");
    
    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertEquals(1, instanceList.size());
    
    ClientResource client = getAuthenticatedClient("process-instance/" + instance.getProcessInstanceId() + "/message/aMessage");
    Representation response = client.post(null);
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.get("success").getBooleanValue());
    
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertEquals(0, instanceList.size());
  }
    
  @Deployment
  public void testMessageEventWithVariables() throws Exception {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("messageEventProcessWithVariables");
    
    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertEquals(1, instanceList.size());
    
    ClientResource client = getAuthenticatedClient("process-instance/" + instance.getProcessInstanceId() + "/message/aMessage");
    
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode objectNode = objectMapper.createObjectNode();
    objectNode.put("test", "hello");
    objectNode.put("test2", 1);
    
    Representation response = client.post(objectNode);
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertTrue(responseNode.get("success").getBooleanValue());
    
    Task task = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
    assertNotNull(task);
    assertEquals("WaitTask", task.getName());
    
    Map<String, Object> variableMap = runtimeService.getVariables(instance.getProcessInstanceId());
    assertTrue(variableMap.containsKey("test"));
    assertEquals("hello", variableMap.get("test"));
    assertTrue(variableMap.containsKey("test2"));
    assertEquals(1, variableMap.get("test2"));
    
    taskService.complete(task.getId());
    
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertEquals(0, instanceList.size());
  }

	@Deployment
	public void testMessageEventSubProcess() throws Exception {
		ProcessInstance instance = runtimeService.startProcessInstanceByKey("messageEventProcessWithSubProcess");

		List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
		assertEquals(1, instanceList.size());

		ClientResource client = getAuthenticatedClient("process-instance/" + instance.getProcessInstanceId() + "/message/aMessage");
		Representation response = client.post(null);
		JsonNode responseNode = objectMapper.readTree(response.getStream());
		assertNotNull(responseNode);
		assertTrue(responseNode.get("success").getBooleanValue());

		Task task = taskService.createTaskQuery().taskName("subProcessTask").singleResult();
		assertNotNull(task);
		taskService.complete(task.getId());

		instanceList = runtimeService.createProcessInstanceQuery().list();
		assertEquals(0, instanceList.size());
	}
}
