package org.activiti.rest.service.api.management;

import java.util.Calendar;

import org.activiti.engine.impl.cmd.ChangeDeploymentTenantIdCmd;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to the Job collection and a single
 * job resource.
 * 
 * @author Frederik Heremans
 */
public class JobResourceTest extends BaseRestTestCase {

  
  /**
   * Test getting a single job. 
   */
  @Deployment(resources = {"org/activiti/rest/service/api/management/JobResourceTest.testTimerProcess.bpmn20.xml"})
  public void testGetJob() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerProcess");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(timerJob);
    
    Calendar now = Calendar.getInstance();
    now.set(Calendar.MILLISECOND, 0);
    processEngineConfiguration.getClock().setCurrentTime(now.getTime());
    
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, timerJob.getId()));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(timerJob.getId(), responseNode.get("id").textValue());
    assertEquals(timerJob.getExceptionMessage(), responseNode.get("exceptionMessage").textValue());
    assertEquals(timerJob.getExecutionId(), responseNode.get("executionId").textValue());
    assertEquals(timerJob.getProcessDefinitionId(), responseNode.get("processDefinitionId").textValue());
    assertEquals(timerJob.getProcessInstanceId(), responseNode.get("processInstanceId").textValue());
    assertEquals(timerJob.getRetries(), responseNode.get("retries").intValue());
    assertEquals(timerJob.getDuedate(), getDateFromISOString(responseNode.get("dueDate").textValue()));
    assertEquals(responseNode.get("tenantId").textValue(), "");
    response.release();
    
    // Set tenant on deployment
    managementService.executeCommand(new ChangeDeploymentTenantIdCmd(deploymentId, "myTenant"));
    
    response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals("myTenant", responseNode.get("tenantId").textValue());
  }
  
  /**
   * Test getting an unexisting job.
   */
  public void testGetUnexistingJob() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, "unexistingjob"));
    
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a job with id 'unexistingjob'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test executing a single job. 
   */
  @Deployment(resources = {"org/activiti/rest/service/api/management/JobResourceTest.testTimerProcess.bpmn20.xml"})
  public void testExecuteJob() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerProcess");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(timerJob);
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "execute");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, timerJob.getId()));
    Representation response = client.post(requestNode);
    assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
    assertEquals(0L, response.getSize());
    
    // Job should be executed
    assertNull(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult());
  }
  
  /**
   * Test executing an unexisting job. 
   */
  @Deployment(resources = {"org/activiti/rest/service/api/management/JobResourceTest.testTimerProcess.bpmn20.xml"})
  public void testExecuteUnexistingJob() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerProcess");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(timerJob);
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "execute");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, "unexistingjob"));
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a job with id 'unexistingjob'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test executing an unexisting job. 
   */
  @Deployment(resources = {"org/activiti/rest/service/api/management/JobResourceTest.testTimerProcess.bpmn20.xml"})
  public void testIllegalActionOnJob() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerProcess");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(timerJob);
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("action", "unexistinAction");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, timerJob.getId()));
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Invalid action, only 'execute' is supported.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test deleting a single job. 
   */
  @Deployment(resources = {"org/activiti/rest/service/api/management/JobResourceTest.testTimerProcess.bpmn20.xml"})
  public void testDeleteJob() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerProcess");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(timerJob);
    
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, timerJob.getId()));
    Representation response = client.delete();
    assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
    assertEquals(0L, response.getSize());
    
    // Job should be deleted
    assertNull(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult());
  }
  
  /**
   * Test getting an unexisting job.
   */
  public void testDeleteUnexistingJob() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, "unexistingjob"));
    
    try {
      client.delete();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a job with id 'unexistingjob'.", expected.getStatus().getDescription());
    }
  }
}
