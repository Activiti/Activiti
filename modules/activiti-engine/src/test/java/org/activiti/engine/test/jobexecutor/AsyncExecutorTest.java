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

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.test.JobTestHelper;
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
			try {
				waitForAllJobsBeingExecuted(processEngine, 500L);
				Assert.fail();
			} catch (ActivitiException e) {
				// Expected
			}
			Assert.assertEquals(1, processEngine.getTaskService().createTaskQuery().taskName("The Task").count());
			Assert.assertEquals(0, processEngine.getTaskService().createTaskQuery().taskName("Task after timer").count());
			Assert.assertEquals(1, processEngine.getManagementService().createJobQuery().count());
			Assert.assertEquals(0, getAsyncExecutorJobCount(processEngine));
	
			// Move clock 3 minutes and 1 second. Triggers the timer
			addSecondsToCurrentTime(processEngine, 181); 
			waitForAllJobsBeingExecuted(processEngine);
	
			// Verify if all is as expected
			Assert.assertEquals(0, processEngine.getTaskService().createTaskQuery().taskName("The Task").count());
			Assert.assertEquals(1, processEngine.getTaskService().createTaskQuery().taskName("Task after timer").count());
			Assert.assertEquals(0, processEngine.getManagementService().createJobQuery().count());
			
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
			Assert.assertEquals(1, firstProcessEngine.getManagementService().createJobQuery().count());
			
			// Create second engine, with async executor enabled. Same time as the first engine to start, then add 301 seconds
			secondProcessEngine = createProcessEngine(true, now);
			addSecondsToCurrentTime(secondProcessEngine, 361); 
			waitForAllJobsBeingExecuted(secondProcessEngine);
	
			// Verify if all is as expected
			Assert.assertEquals(0, firstProcessEngine.getTaskService().createTaskQuery().taskName("The Task").count());
			Assert.assertEquals(1, firstProcessEngine.getTaskService().createTaskQuery().taskName("Task after timer").count());
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
			processEngine = createProcessEngine(true);
			processEngine.getProcessEngineConfiguration().getClock().reset();
			deploy(processEngine,"AsyncExecutorTest.testAsyncFailingScript.bpmn20.xml");
	
			// Start process instance. Wait for all jobs to be done.
			processEngine.getRuntimeService().startProcessInstanceByKey("asyncScript");
			
			// There is a back off mechanism for the retry, so need a bit of time
			// But to be sure, we make the wait time small 
			processEngine.getProcessEngineConfiguration().setAsyncFailedJobWaitTime(1);
			
			final ProcessEngine processEngineCopy = processEngine;
			JobTestHelper.waitForJobExecutorOnCondition(processEngine.getProcessEngineConfiguration(), 10000L, 2000L, new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return processEngineCopy.getManagementService().createJobQuery().withRetriesLeft().count() == 0;
				}
			});
			
	
			// Verify if all is as expected
			Assert.assertEquals(0, processEngine.getTaskService().createTaskQuery().taskName("Task after script").count());
			Assert.assertEquals(1, processEngine.getManagementService().createJobQuery().count());
			Assert.assertEquals(0, processEngine.getManagementService().createJobQuery().withRetriesLeft().count());
			Assert.assertEquals(1, processEngine.getManagementService().createJobQuery().noRetriesLeft().count());
			Assert.assertEquals(1, processEngine.getManagementService().createJobQuery().withException().count());
	
			Assert.assertEquals(3, getAsyncExecutorJobCount(processEngine));
			
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
		processEngineConfiguration.setJobExecutorActivate(false); // No need for that old job executor
		
		if (enableAsyncExecutor) {
			processEngineConfiguration.setAsyncExecutorEnabled(true);
			processEngineConfiguration.setAsyncExecutorActivate(true);
			
			CountingAsyncExecutor countingAsyncExecutor = new CountingAsyncExecutor();
			countingAsyncExecutor.setDefaultAsyncJobAcquireWaitTimeInMillis(50); // To avoid waiting too long when a retry happens
			countingAsyncExecutor.setDefaultTimerJobAcquireWaitTimeInMillis(50);
			processEngineConfiguration.setAsyncExecutor(countingAsyncExecutor);
		}

		ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
		
		if (time != null) {
			processEngine.getProcessEngineConfiguration().getClock().setCurrentTime(time);
		}
		
		return processEngine;
	}
	
	private Date setClockToCurrentTime(ProcessEngine processEngine) {
		Date date = new Date();
		processEngine.getProcessEngineConfiguration().getClock().setCurrentTime(date);
		return date;
	}
	
	private void addSecondsToCurrentTime(ProcessEngine processEngine, long nrOfSeconds) {
		Date currentTime = processEngine.getProcessEngineConfiguration().getClock().getCurrentTime();
		processEngine.getProcessEngineConfiguration().getClock().setCurrentTime(new Date(currentTime.getTime() + (nrOfSeconds * 1000L)));
	}
	
	private void cleanup(ProcessEngine processEngine) {
		for (org.activiti.engine.repository.Deployment deployment : processEngine.getRepositoryService().createDeploymentQuery().list()) {
			processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);
		}
		processEngine.close();
	}
	
	private String deploy(ProcessEngine processEngine, String resource) {
		return processEngine.getRepositoryService().createDeployment().addClasspathResource("org/activiti/engine/test/jobexecutor/" + resource).deploy().getId();
	}

	private void waitForAllJobsBeingExecuted(ProcessEngine processEngine) {
		waitForAllJobsBeingExecuted(processEngine, 10000L);
  }
	
	private void waitForAllJobsBeingExecuted(ProcessEngine processEngine, long maxWaitTime) {
	  JobTestHelper.waitForJobExecutorToProcessAllJobs(processEngine.getProcessEngineConfiguration(), processEngine.getManagementService(), maxWaitTime, 1000L, false);
  }
	
	private int getAsyncExecutorJobCount(ProcessEngine processEngine) {
		AsyncExecutor asyncExecutor = processEngine.getProcessEngineConfiguration().getAsyncExecutor();
		if (asyncExecutor != null) {
			return ((CountingAsyncExecutor) asyncExecutor).getCounter().get();
		}
		return 0;
	}
	
	static class CountingAsyncExecutor extends DefaultAsyncJobExecutor {
		
		private static final Logger logger = LoggerFactory.getLogger(CountingAsyncExecutor.class);
		
		private AtomicInteger counter = new AtomicInteger(0);
		
		@Override
		public boolean executeAsyncJob(JobEntity job) {
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
