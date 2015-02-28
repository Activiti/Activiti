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
package org.activiti.engine.test.jobexecutor;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;

/**
 * @author Saeid Mirzaei
 */

public class JobExecutorFailRetryTest extends PluggableActivitiTestCase {
	
	@Deployment
	public void testFailedServiceTask() {
		
		// process throws no exception. Service task passes at the first time. 
		RetryFailingDelegate.shallThrow = false;  // do not throw exception in Service delegate
		RetryFailingDelegate.resetTimeList();
  	runtimeService.startProcessInstanceByKey("failedJobRetry");
  	
  	waitForJobExecutorToProcessAllJobs(600, 200);
  	assertEquals(1, RetryFailingDelegate.times.size());  // check number of calls of delegate
  	
  	// process throws exception two times, with 6 seconds in between
  	RetryFailingDelegate.shallThrow = true;  // throw exception in Service delegate
		RetryFailingDelegate.resetTimeList();
  	runtimeService.startProcessInstanceByKey("failedJobRetry");
  	
  	executeJobExecutorForTime(14000, 500);
  	assertEquals(2, RetryFailingDelegate.times.size());  // check number of calls of delegate
  	long timeDiff = RetryFailingDelegate.times.get(1) - RetryFailingDelegate.times.get(0) ; 
  	assertTrue(timeDiff > 6000 && timeDiff < 12000);  // check time difference between calls. Just roughly
  }
	
	// checks if the proper exception is thrown after all tries are exhausted
  @Deployment
  public void testFailedServiceTaskException() {
	// service task throws exception all the time
  	RetryFailingDelegate.shallThrow = true;  // throw exception in Service delegate
  	RetryFailingDelegate.resetTimeList();
  	RetryFlag.reset();
  	runtimeService.startProcessInstanceByKey("failedJobRetryException");
  	
  	waitForJobExecutorToProcessAllJobs(8000, 500);
  	// check that 2 tries are done
  	assertTrue(RetryFlag.visited);
  	assertEquals(2, RetryFailingDelegate.times.size());  // check number of calls of delegate
  	
  }
  
  // checks if the proper exception is thrown after all tries are exhausted
/*
  @Deployment
  public void testFailedServiceTaskExceptionCancelActivity () {
 
    RetryFailingDelegate.shallThrow = true;  // throw exception in Service delegate
    RetryFailingDelegate.resetTimeList();
    RetryFlag.reset();
    runtimeService.startProcessInstanceByKey("failedJobRetryException");
    
    waitForJobExecutorToProcessAllJobs(8000, 500);
    assertTrue(RetryFlag.visited);
    assertEquals(2, RetryFailingDelegate.times.size());  // check number of calls of delegate
    
  }
  
  */
  @Deployment
  public void testFailedServiceTaskExceptionInParallelBranch() {
     RetryFailingDelegate.resetTimeList();
     RetryFailingDelegate.shallThrow = true;
     runtimeService.startProcessInstanceByKey("failedRetryExceptionInBranch");
     waitForJobExecutorToProcessAllJobs(8000, 500);
     assertTrue(RetryFlag.visited);
   
  }
  
}