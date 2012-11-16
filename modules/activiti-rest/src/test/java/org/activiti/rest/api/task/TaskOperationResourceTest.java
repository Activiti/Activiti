package org.activiti.rest.api.task;

import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.codehaus.jackson.JsonNode;
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
}
