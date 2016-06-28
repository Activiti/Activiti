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
package org.activiti5.engine.test.jobexecutor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.repository.DeploymentProperties;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti5.engine.impl.test.JobTestHelper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests specifically for the {@link AsyncExecutor}.
 * 
 * Note that all tests with jobs already use this async executor, so this
 * is really all the 'edgy' cases here. 
 * 
 * @author jbarrez
 */
public class AsyncExecutorTest {
	
	@Test
	public void testRegularAsyncExecution() {
		
		ProcessEngine processEngine = null;
		
		try {
			// Deploy
			processEngine = createProcessEngine(true);
			setClockToCurrentTime(processEngine);
			deploy(processEngine, "AsyncExecutorTest.testRegularAsyncExecution.bpmn20.xml");
			
			// Start process instance. Wait for all jobs to be done
			processEngine.getRuntimeService().startProcessInstanceByKey("asyncExecutor");
			
			// Move clock 3 minutes. Nothing should happen
			addSecondsToCurrentTime(processEngine, 180L);
			waitForAllJobsBeingExecuted(processEngine, 2000L);
			
			Assert.assertEquals(1, processEngine.getTaskService().createTaskQuery().taskName("The Task").count());
			Assert.assertEquals(0, processEngine.getTaskService().createTaskQuery().taskName("Task after timer").count());
			Assert.assertEquals(1, processEngine.getManagementService().createTimerJobQuery().count());
			Assert.assertEquals(0, getAsyncExecutorJobCount(processEngine));
	
			// Move clock 3 minutes and 1 second. Triggers the timer
			addSecondsToCurrentTime(processEngine, 181); 
			waitForAllJobsBeingExecuted(processEngine);
	
			// Verify if all is as expected
			Assert.assertEquals(0, processEngine.getTaskService().createTaskQuery().taskName("The Task").count());
			Assert.assertEquals(1, processEngine.getTaskService().createTaskQuery().taskName("Task after timer").count());
			Assert.assertEquals(0, processEngine.getManagementService().createTimerJobQuery().count());
			
			Assert.assertEquals(1, getAsyncExecutorJobCount(processEngine));
			
		} finally {
		
			// Clean up
			if (processEngine != null) {
				cleanup(processEngine);
			}
		}
	}
	
	@Test
	public void testAsyncExecutorDisabledOnOneEngine() {
		
		ProcessEngine firstProcessEngine = null;
		ProcessEngine secondProcessEngine = null;
		
		try {
		
			// Deploy on one engine, where the async executor is disabled
			firstProcessEngine = createProcessEngine(false);
			Date now = setClockToCurrentTime(firstProcessEngine);
			deploy(firstProcessEngine, "AsyncExecutorTest.testRegularAsyncExecution.bpmn20.xml");
			
			// Start process instance on first engine
			firstProcessEngine.getRuntimeService().startProcessInstanceByKey("asyncExecutor");
			
			//	Move clock 5 minutes and 1 second. Triggers the timer normally, but not now since async execution is disabled
			addSecondsToCurrentTime(firstProcessEngine, 301); // 301 = 5m01s
			Assert.assertEquals(1, firstProcessEngine.getTaskService().createTaskQuery().taskName("The Task").count());
			Assert.assertEquals(0, firstProcessEngine.getTaskService().createTaskQuery().taskName("Task after timer").count());
			Assert.assertEquals(1, firstProcessEngine.getManagementService().createTimerJobQuery().count());
			
			// Create second engine, with async executor enabled. Same time as the first engine to start, then add 301 seconds
			secondProcessEngine = createProcessEngine(true, now);
			addSecondsToCurrentTime(secondProcessEngine, 361); 
			waitForAllJobsBeingExecuted(secondProcessEngine);
	
			// Verify if all is as expected
			Assert.assertEquals(0, firstProcessEngine.getTaskService().createTaskQuery().taskName("The Task").count());
			Assert.assertEquals(1, firstProcessEngine.getTaskService().createTaskQuery().taskName("Task after timer").count());
			Assert.assertEquals(0, firstProcessEngine.getManagementService().createTimerJobQuery().count());
			Assert.assertEquals(0, firstProcessEngine.getManagementService().createJobQuery().count());
			
			Assert.assertEquals(0, getAsyncExecutorJobCount(firstProcessEngine));
			Assert.assertEquals(1, getAsyncExecutorJobCount(secondProcessEngine));
		
		} finally {
			
			// Clean up
			cleanup(firstProcessEngine);
			cleanup(secondProcessEngine);
			
		}
		
	}
	
	@Test
	public void testAsyncScriptExecution() {
		
		ProcessEngine processEngine = null;
		
		try {

			// Deploy
			processEngine = createProcessEngine(true);
			setClockToCurrentTime(processEngine);
			deploy(processEngine,"AsyncExecutorTest.testAsyncScriptExecution.bpmn20.xml");
	
			// Start process instance. Wait for all jobs to be done
			processEngine.getRuntimeService().startProcessInstanceByKey("asyncScript");
			waitForAllJobsBeingExecuted(processEngine);
	
			// Verify if all is as expected
			Assert.assertEquals(1, processEngine.getTaskService().createTaskQuery().taskName("Task after script").count());
			Assert.assertEquals(0, processEngine.getManagementService().createJobQuery().count());
			Assert.assertEquals(0, processEngine.getManagementService().createTimerJobQuery().count());
	
			Assert.assertEquals(1, getAsyncExecutorJobCount(processEngine));
			
		} finally {
		
			// Clean up
			cleanup(processEngine);
			
		}
		
	}
	
	@Test
	public void testAsyncScriptExecutionOnTwoEngines() {
		
		ProcessEngine firstProcessEngine = null;
		ProcessEngine secondProcessEngine = null;
		
		try {

			// Deploy
			firstProcessEngine = createProcessEngine(false);
			Date now = setClockToCurrentTime(firstProcessEngine);
			deploy(firstProcessEngine,"AsyncExecutorTest.testAsyncScriptExecution.bpmn20.xml");
	
			// Start process instance. Nothing should happen
			firstProcessEngine.getRuntimeService().startProcessInstanceByKey("asyncScript");
			Assert.assertEquals(0, firstProcessEngine.getTaskService().createTaskQuery().taskName("Task after script").count());
			Assert.assertEquals(1, firstProcessEngine.getManagementService().createJobQuery().count());
			
			// Start second engine, with async executor enabled
			secondProcessEngine = createProcessEngine(true, now); // Same timestamp as first engine
			Assert.assertEquals(0, firstProcessEngine.getTaskService().createTaskQuery().taskName("Task after script").count());
			Assert.assertEquals(1, firstProcessEngine.getManagementService().createJobQuery().count());
			
			// Move the clock 1 second. Should be executed now by second engine
			addSecondsToCurrentTime(secondProcessEngine, 1);
			waitForAllJobsBeingExecuted(secondProcessEngine, 10000L);
	
			// Verify if all is as expected
			Assert.assertEquals(1, firstProcessEngine.getTaskService().createTaskQuery().taskName("Task after script").count());
			Assert.assertEquals(0, firstProcessEngine.getManagementService().createJobQuery().count());
	
			Assert.assertEquals(0, getAsyncExecutorJobCount(firstProcessEngine));
			Assert.assertEquals(1, getAsyncExecutorJobCount(secondProcessEngine));
			
		} finally {
			
			// Clean up
			cleanup(firstProcessEngine);
			cleanup(secondProcessEngine);
			
		}
	}
	
	@Test
	public void testAsyncFailingScript() {
		ProcessEngine processEngine = null;
		
		try {
		
			// Deploy
			processEngine = createProcessEngine(false);
			ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
			processEngineConfig.resetClock();
			deploy(processEngine,"AsyncExecutorTest.testAsyncFailingScript.bpmn20.xml");
	
			// Start process instance. Wait for all jobs to be done.
			ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("asyncScript");
			
			Clock clock = processEngineConfig.getClock();
			clock.reset();
			
			try {
  			processEngine.getManagementService().executeJob(processEngine.getManagementService().createJobQuery()
  			    .processInstanceId(processInstance.getId())
  			    .singleResult().getId());
  			fail("Job execution should have failed");
			} catch (ActivitiException e) {
			  // expected
			}
			
			Job job = findJobsToExecute(processEngineConfig);
			assertNull(job);
			
			// retry should be 2 now
			Calendar newCal = Calendar.getInstance();
			newCal.setTime(clock.getCurrentTime());
			newCal.add(Calendar.SECOND, 15);
			clock.setCurrentCalendar(newCal);
			processEngineConfig.setClock(clock);
			
			job = findJobsToExecute(processEngineConfig);
			assertNotNull(job);
			assertEquals(2, job.getRetries());
			try {
			  processEngine.getManagementService().moveTimerToExecutableJob(job.getId());
			  processEngine.getManagementService().executeJob(job.getId());
			  fail("Job execution should have failed");
      } catch (ActivitiException e) {
        // expected
      }
			
			job = findJobsToExecute(processEngineConfig);
      assertNull(job);
      
      // retry should be 1 now
      newCal = Calendar.getInstance();
      newCal.setTime(clock.getCurrentTime());
      newCal.add(Calendar.MINUTE, 10);
      clock.setCurrentCalendar(newCal);
      processEngineConfig.setClock(clock);
      
      job = findJobsToExecute(processEngineConfig);
      assertNotNull(job);
      assertEquals(1, job.getRetries());
      try {
        processEngine.getManagementService().moveTimerToExecutableJob(job.getId());
        processEngine.getManagementService().executeJob(job.getId());
        fail("Job execution should have failed");
      } catch (ActivitiException e) {
        // expected
      }
      
      job = processEngine.getManagementService().createDeadLetterJobQuery().processInstanceId(processInstance.getId()).singleResult();
      assertNotNull(job);
      
			// Verify if all is as expected
			Assert.assertEquals(0, processEngine.getTaskService().createTaskQuery().taskName("Task after script").count());
			Assert.assertEquals(0, processEngine.getManagementService().createJobQuery().count());
			Assert.assertEquals(1, processEngine.getManagementService().createDeadLetterJobQuery().count());
			
			// all job retries are handled by Activiti 5 job retry so expected value is 0
			Assert.assertEquals(0, getAsyncExecutorJobCount(processEngine));
			
		} finally {
			
			// Clean up
			cleanup(processEngine);
		}
		
	}
	
	// Helpers
	
	private ProcessEngine createProcessEngine(boolean enableAsyncExecutor) {
		return createProcessEngine(enableAsyncExecutor, null);
	}
	
	private ProcessEngine createProcessEngine(boolean enableAsyncExecutor, Date time) {
		ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
		processEngineConfiguration.setJdbcUrl("jdbc:h2:mem:activiti-AsyncExecutorTest;DB_CLOSE_DELAY=1000");
		processEngineConfiguration.setDatabaseSchemaUpdate("true");
		processEngineConfiguration.setActiviti5CompatibilityEnabled(true);
		
		if (enableAsyncExecutor) {
			processEngineConfiguration.setAsyncExecutorActivate(true);
			
			CountingAsyncExecutor countingAsyncExecutor = new CountingAsyncExecutor();
	    countingAsyncExecutor.setDefaultAsyncJobAcquireWaitTimeInMillis(100); // To avoid waiting too long when a retry happens
	    countingAsyncExecutor.setDefaultTimerJobAcquireWaitTimeInMillis(100);
	    processEngineConfiguration.setAsyncExecutor(countingAsyncExecutor);
		}

		ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
		
		if (time != null) {
		  processEngine.getProcessEngineConfiguration().getClock().setCurrentTime(time);
		}
		
		return processEngine;
	}
	
	protected Job findJobsToExecute(ProcessEngineConfigurationImpl processEngineConfig) {
	  Job result = null;
    List<Job> jobs = processEngineConfig.getManagementService().createTimerJobQuery().executable().list();
    if (jobs != null && jobs.size() > 0) {
      result = jobs.get(0);
    }
    return result;
	}
	
	private Date setClockToCurrentTime(ProcessEngine processEngine) {
		Clock clock = processEngine.getProcessEngineConfiguration().getClock();
		((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).resetClock();
		Date date = clock.getCurrentTime();
		return date;
	}
	
	private void addSecondsToCurrentTime(ProcessEngine processEngine, long nrOfSeconds) {
	  Clock clock = processEngine.getProcessEngineConfiguration().getClock();
	  clock.setCurrentTime(new Date(clock.getCurrentTime().getTime() + (nrOfSeconds * 1000L)));
		processEngine.getProcessEngineConfiguration().setClock(clock);
	}
	
	private void cleanup(ProcessEngine processEngine) {
		for (org.activiti.engine.repository.Deployment deployment : processEngine.getRepositoryService().createDeploymentQuery().list()) {
			processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);
		}
		processEngine.close();
	}
	
	private String deploy(ProcessEngine processEngine, String resource) {
		return processEngine.getRepositoryService().createDeployment().addClasspathResource("org/activiti5/engine/test/jobexecutor/" + resource)
		    .deploymentProperty(DeploymentProperties.DEPLOY_AS_ACTIVITI5_PROCESS_DEFINITION, Boolean.TRUE)
		    .deploy()
		    .getId();
	}

	private void waitForAllJobsBeingExecuted(ProcessEngine processEngine) {
		waitForAllJobsBeingExecuted(processEngine, 10000L);
  }
	
	private void waitForAllJobsBeingExecuted(ProcessEngine processEngine, long maxWaitTime) {
	  JobTestHelper.waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(processEngine.getProcessEngineConfiguration(), 
	      processEngine.getManagementService(), maxWaitTime, 500L, false);
  }
	
	private int getAsyncExecutorJobCount(ProcessEngine processEngine) {
	  AsyncExecutor asyncExecutor = processEngine.getProcessEngineConfiguration().getAsyncExecutor();
	  if (asyncExecutor instanceof CountingAsyncExecutor) {
			return ((CountingAsyncExecutor) asyncExecutor).getCounter().get();
		}
		return 0;
	}
	
	static class CountingAsyncExecutor extends DefaultAsyncJobExecutor {
		
		private static final Logger logger = LoggerFactory.getLogger(CountingAsyncExecutor.class);
		
		private AtomicInteger counter = new AtomicInteger(0);
		
		@Override
		public boolean executeAsyncJob(Job job) {
			logger.info("About to execute job " + job.getId());
			counter.incrementAndGet();
		  boolean success = super.executeAsyncJob(job);
		  logger.info("Handed off job " + job.getId() + " to async executor (retries=" + job.getRetries() + ")");
		  return success;
		}

		public AtomicInteger getCounter() {
			return counter;
		}

		public void setCounter(AtomicInteger counter) {
			this.counter = counter;
		}
		
	}
	
	
}
