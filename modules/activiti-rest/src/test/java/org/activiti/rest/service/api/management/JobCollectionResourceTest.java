package org.activiti.rest.service.api.management;

import java.util.Calendar;
import java.util.Collections;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Test for all REST-operations related to the Job collection and a single
 * job resource.
 * 
 * @author Frederik Heremans
 */
public class JobCollectionResourceTest extends BaseRestTestCase {
  
  @Deployment(resources = {"org/activiti/rest/service/api/management/JobCollectionResourceTest.testTimerProcess.bpmn20.xml"})
  public void testGetJobs() throws Exception {
    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR, -1);
    
    Calendar inAnHour = Calendar.getInstance();
    inAnHour.add(Calendar.HOUR, 1);
    
    // Start process, forcing error on job-execution
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerProcess", 
            Collections.singletonMap("error", (Object) Boolean.TRUE));
    
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).timers().singleResult();
    assertNotNull(timerJob);
    
    for(int i=0; i < timerJob.getRetries(); i++) {
      // Force execution of job until retries are exhausted
      try {
        managementService.executeJob(timerJob.getId());
        fail();
      } catch(ActivitiException expected) {
        // Ignore, we expect the exception
      }
    }
    timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).timers().singleResult();
    assertEquals(0, timerJob.getRetries());
    
    // Fetch the async-job (which has retries left)
    Job asyncJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).withRetriesLeft().singleResult();
    
    // Test fetching all jobs
    String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION);
    assertResultsPresentInDataResponse(url, asyncJob.getId(), timerJob.getId());
    
    // Fetch using job-id
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?id=" + asyncJob.getId();
    assertResultsPresentInDataResponse(url, asyncJob.getId());
    
    // Fetch using processInstanceId
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?processInstanceId=" + processInstance.getId();
    assertResultsPresentInDataResponse(url, asyncJob.getId(), timerJob.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?processInstanceId=unexisting";
    assertResultsPresentInDataResponse(url);
    
    // Fetch using executionId
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?executionId=" + asyncJob.getExecutionId();
    assertResultsPresentInDataResponse(url, asyncJob.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?executionId=" + timerJob.getExecutionId();
    assertResultsPresentInDataResponse(url, timerJob.getId());
    
    // Fetch using processDefinitionId
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?processDefinitionId=" + processInstance.getProcessDefinitionId();
    assertResultsPresentInDataResponse(url, asyncJob.getId(), timerJob.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?processDefinitionId=unexisting";
    assertResultsPresentInDataResponse(url);
    
    // Fetch using withRetriesLeft
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?withRetriesLeft=true";
    assertResultsPresentInDataResponse(url, asyncJob.getId());
    
    // Fetch using executable
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?executable=true";
    assertResultsPresentInDataResponse(url, asyncJob.getId());
    
    // Fetch using timers only
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?timersOnly=true";
    assertResultsPresentInDataResponse(url, timerJob.getId());
    
    // Combining messagesOnly with timersOnly should result in exception
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION)
            + "?timersOnly=true&messagesOnly=true");
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Only one of 'timersOnly' or 'messagesOnly' can be provided.", expected.getStatus().getDescription());
    }
    
    // Fetch using dueBefore
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?dueBefore=" + getISODateString(inAnHour.getTime());
    assertResultsPresentInDataResponse(url, timerJob.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?dueBefore=" + getISODateString(hourAgo.getTime());
    assertResultsPresentInDataResponse(url);
    
    // Fetch using dueAfter
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?dueAfter=" + getISODateString(hourAgo.getTime());
    assertResultsPresentInDataResponse(url, timerJob.getId());
    
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?dueAfter=" + getISODateString(inAnHour.getTime());
    assertResultsPresentInDataResponse(url);
    
    // Fetch using withException
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?withException=true";
    assertResultsPresentInDataResponse(url, timerJob.getId());
    
    // Fetch with exceptionMessage
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?exceptionMessage=" + timerJob.getExceptionMessage();
    assertResultsPresentInDataResponse(url, timerJob.getId());
    
    // Fetch with empty exceptionMessage
    url = RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_COLLECTION) + "?exceptionMessage=";
    assertResultsPresentInDataResponse(url);
  }
}