package org.activiti.rest.service.api.management;

import java.util.Calendar;
import java.util.Collections;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

/**
 * Test for all REST-operations related to the Job collection and a single
 * job resource.
 * 
 * @author Frederik Heremans
 */
public class JobExceptionStacktraceResourceTest extends BaseSpringRestTestCase {

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
    processEngineConfiguration.getClock().setCurrentTime(now.getTime());
    
    CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_EXCEPTION_STRACKTRACE, timerJob.getId())), HttpStatus.SC_OK);
    
    String stack = IOUtils.toString(response.getEntity().getContent());
    assertNotNull(stack);
    assertEquals(managementService.getJobExceptionStacktrace(timerJob.getId()), stack);
    
    // Also check content-type
    assertEquals("text/plain", response.getEntity().getContentType().getValue());
    closeResponse(response);
  }
  
  /**
   * Test getting the stacktrace for an unexisting job.
   */
  public void testGetStrackForUnexistingJob() throws Exception {
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_EXCEPTION_STRACKTRACE, "unexistingjob")), HttpStatus.SC_NOT_FOUND));
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
    
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_JOB_EXCEPTION_STRACKTRACE, timerJob.getId())), HttpStatus.SC_NOT_FOUND));
  }
}
