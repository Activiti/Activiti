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

package org.activiti.engine.test.api.mgmt;

import junit.framework.Assert;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


/**
 * @author Frederik Heremans
 */
public class ManagementServiceTest extends ActivitiInternalTestCase {

  public void testGetMetaDataForUnexistingTable() {
    TableMetaData metaData = managementService.getTableMetaData("unexistingtable");
    assertNull(metaData);
  }
  
  public void testGetMetaDataNullTableName() {
    try {
      managementService.getTableMetaData(null);
      fail("ActivitiException expected");
    } catch (ActivitiException re) {
      assertTextPresent("tableName is null", re.getMessage());
    }
  }
  
  public void testExecuteJobNullJobId() {
    try {
      managementService.executeJob(null);
      fail("ActivitiException expected");
    } catch (ActivitiException re) {
      assertTextPresent("jobId is null", re.getMessage());
    }
  }
  
  public void testExecuteJobUnexistingJob() {
    try {
      managementService.executeJob("unexistingjob");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("No job found with id", ae.getMessage());
    }
  }
  
  
  @Deployment
  public void testGetJobExceptionStacktrace() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");
    
    // The execution is waiting in the first usertask. This contains a boundry
    // timer event which we will execute manual for testing purposes.
    Job timerJob = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();
    
    assertNotNull("No job found for process instance", timerJob);
    
    try {
      managementService.executeJob(timerJob.getId());
      fail("RuntimeException from within the script task expected");
    } catch(RuntimeException re) {
      assertTextPresent("This is an exception thrown from scriptTask", re.getMessage());
    }
    
    // Fetch the task to see that the exception that occurred is persisted
    timerJob = managementService.createJobQuery()
    .processInstanceId(processInstance.getId())
    .singleResult();
    
    Assert.assertNotNull(timerJob);
    Assert.assertNotNull(timerJob.getExceptionMessage());
    assertTextPresent("This is an exception thrown from scriptTask", timerJob.getExceptionMessage());
    
    // Get the full stacktrace using the managementService
    String exceptionStack = managementService.getJobExceptionStacktrace(timerJob.getId());
    Assert.assertNotNull(exceptionStack);
    assertTextPresent("This is an exception thrown from scriptTask", exceptionStack);    
  }
  
  public void testgetJobExceptionStacktraceUnexistingJobId() {
    try {
      managementService.getJobExceptionStacktrace("unexistingjob");
      fail("ActivitiException expected");
    } catch (ActivitiException re) {
      assertTextPresent("No job found with id unexistingjob", re.getMessage());
    }
  }
  
  public void testgetJobExceptionStacktraceNullJobId() {
    try {
      managementService.getJobExceptionStacktrace(null);
      fail("ActivitiException expected");
    } catch (ActivitiException re) {
      assertTextPresent("jobId is null", re.getMessage());
    }
  }
}
