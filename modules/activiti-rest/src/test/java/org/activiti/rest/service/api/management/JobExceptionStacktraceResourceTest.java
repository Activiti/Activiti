package org.activiti.rest.service.api.management;

import java.util.Calendar;
import java.util.Collections;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.io.IOUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Test for all REST-operations related to the Job collection and a single
 * job resource.
 * 
 * @author Frederik Heremans
 */
public class JobExceptionStacktraceResourceTest extends BaseRestTestCase {

  
  /**
   * Test getting the stacktrace for a failed job
   */
  @Deployment(resources = {"org/activiti/rest/service/api/management/JobExceptionStacktraceResourceTest.testTimerProcess.bpmn20.xml"})
  public void testGetJobStacktrace() throws Exception {
    // Start process, forcing error on job-execution
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerProcess", 
            Collections.singletonMap("error", (Object) Boolean.TRUE));
    
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(timerJob);
    
    // Force execution of job
    try {
      managementService.executeJob(timerJob.getId());
      fail();
    } catch(ActivitiException expected) {
      // Ignore, we expect the exception
    }
    
    Calendar now = Calendar.getInstance();
    now.set(Calendar.MILLISECOND, 0);
    ClockUtil.setCurrentTime(now.getTime());
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_EXCEPTION_STRACKTRACE, timerJob.getId()));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    String stack = IOUtils.toString(response.getStream());
    assertNotNull(stack);
    assertEquals(managementService.getJobExceptionStacktrace(timerJob.getId()), stack);
    
    // Also check content-type
    assertTrue(getMediaType(client).contains(MediaType.TEXT_PLAIN.getName()));
   
  }
  
  /**
   * Test getting the stacktrace for an unexisting job.
   */
  public void testGetStrackForUnexistingJob() throws Exception {
    ClientResource client = getAuthenticatedClient(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_EXCEPTION_STRACKTRACE, "unexistingjob"));
    
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a job with id 'unexistingjob'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test getting the stacktrace for an unexisting job.
   */
  @Deployment(resources = {"org/activiti/rest/service/api/management/JobExceptionStacktraceResourceTest.testTimerProcess.bpmn20.xml"})
  public void testGetStrackForJobWithoutException() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerProcess", 
            Collections.singletonMap("error", (Object) Boolean.FALSE));
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(timerJob);
    
    ClientResource client = getAuthenticatedClient(
            RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_EXCEPTION_STRACKTRACE, timerJob.getId()));
    
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Job with id '" + timerJob.getId() + "' doesn't have an exception stacktrace.", expected.getStatus().getDescription());
    }
  }
  

}