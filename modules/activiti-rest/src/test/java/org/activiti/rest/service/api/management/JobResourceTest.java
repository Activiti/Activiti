package org.activiti.rest.service.api.management;

import java.util.Calendar;

import org.activiti.engine.impl.cmd.ChangeDeploymentTenantIdCmd;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test for all REST-operations related to the Job collection and a single
 * job resource.
 * 
 * @author Frederik Heremans
 */
public class JobResourceTest extends BaseSpringRestTestCase {

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
    
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, timerJob.getId())), HttpStatus.SC_OK);
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals(timerJob.getId(), responseNode.get("id").textValue());
    assertEquals(timerJob.getExceptionMessage(), responseNode.get("exceptionMessage").textValue());
    assertEquals(timerJob.getExecutionId(), responseNode.get("executionId").textValue());
    assertEquals(timerJob.getProcessDefinitionId(), responseNode.get("processDefinitionId").textValue());
    assertEquals(timerJob.getProcessInstanceId(), responseNode.get("processInstanceId").textValue());
    assertEquals(timerJob.getRetries(), responseNode.get("retries").intValue());
    assertEquals(timerJob.getDuedate(), getDateFromISOString(responseNode.get("dueDate").textValue()));
    assertEquals("", responseNode.get("tenantId").textValue());
    
    // Set tenant on deployment
    managementService.executeCommand(new ChangeDeploymentTenantIdCmd(deploymentId, "myTenant"));
    
    response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, timerJob.getId())), HttpStatus.SC_OK);
    responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
    assertNotNull(responseNode);
    assertEquals("myTenant", responseNode.get("tenantId").textValue());
  }
  
  /**
   * Test getting an unexisting job.
   */
  public void testGetUnexistingJob() throws Exception {
  	CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, "unexistingjob")), HttpStatus.SC_NOT_FOUND);
  	closeResponse(response);
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
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, timerJob.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_NO_CONTENT);
    closeResponse(response);
    
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
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, "unexistingjob"));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_NOT_FOUND);
    closeResponse(response);
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
    
    HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, timerJob.getId()));
    httpPost.setEntity(new StringEntity(requestNode.toString()));
    CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST);
    closeResponse(response);
  }
  
  /**
   * Test deleting a single job. 
   */
  @Deployment(resources = {"org/activiti/rest/service/api/management/JobResourceTest.testTimerProcess.bpmn20.xml"})
  public void testDeleteJob() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerProcess");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(timerJob);
    
    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, timerJob.getId()));
    CloseableHttpResponse response = executeRequest(httpDelete, HttpStatus.SC_NO_CONTENT);
    closeResponse(response);
    
    // Job should be deleted
    assertNull(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult());
  }
  
  /**
   * Test getting an unexisting job.
   */
  public void testDeleteUnexistingJob() throws Exception {
    HttpDelete httpDelete = new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB, "unexistingjob"));
    CloseableHttpResponse response = executeRequest(httpDelete, HttpStatus.SC_NOT_FOUND);
    closeResponse(response);
  }
}
