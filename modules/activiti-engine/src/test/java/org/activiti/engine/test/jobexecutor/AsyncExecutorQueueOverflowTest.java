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
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.junit.Assert;
import org.junit.Test;

/**
 * This test specifically overflows the job queue used by the async executor.
 * This test was written to fix a bug in the async executor, where jobs could be
 * stalled for 5 minutes (the default lock time) when the queue overflowed.
 * 
 * @author Joram Barrez
 */
public class AsyncExecutorQueueOverflowTest {

protected static DataSource dataSource;
  
  @Test
  public void testQueueOverflow() throws Exception {
    
    ProcessEngine processEngine = initProcessEngineWithJobQueueSize(100);
    
    // Start date = Wed 20 january 2016 7:00 GMT
    Date startDate = new Date(1453273200000L);
    processEngine.getProcessEngineConfiguration().getClock().setCurrentTime(startDate);
    
    final RepositoryService repositoryService = processEngine.getRepositoryService();
    final RuntimeService runtimeService = processEngine.getRuntimeService();
    final HistoryService historyService = processEngine.getHistoryService();
    
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/jobexecutor/AsyncExecutorQueueOverflowTest.bpmn20.xml").deploy();
    
    // 300 process instances, each having 1 timer + 2 async jobs = 900 jobs
    int nrOfProcessInstances = 300; 
    
    for (int i=0; i<nrOfProcessInstances; i++) {
      runtimeService.startProcessInstanceByKey("testAsyncExecutor");
    }
    
    Assert.assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    
    // Move date to Monday 9:01, triggering all timers
    Date mondayMorningDate = new Date(1453280460000L);
    processEngine.getProcessEngineConfiguration().getClock().setCurrentTime(mondayMorningDate);
    
    boolean allJobsProcessed = false;
    while (!allJobsProcessed) {
      
      long count = historyService.createHistoricActivityInstanceQuery().activityId("theServiceTask").unfinished().count();
      System.out.println("COUNT = " + count);
      allJobsProcessed = count == nrOfProcessInstances; 
      
      if (!allJobsProcessed) {
        Thread.sleep(1000L);
      }
      
    }
    
    Assert.assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(nrOfProcessInstances, historyService.createHistoricActivityInstanceQuery().activityId("theScriptTask").finished().count());
    Assert.assertEquals(nrOfProcessInstances, historyService.createHistoricActivityInstanceQuery().activityId("theServiceTask").unfinished().count());
    
    processEngine.close();
    
  }

  protected ProcessEngine initProcessEngineWithJobQueueSize(int queueSize) throws Exception{
    StandaloneInMemProcessEngineConfiguration config = new StandaloneInMemProcessEngineConfiguration();
    config.setJdbcUrl("jdbc:h2:mem:activiti-AsyncExecutorQueueOverflowTest;DB_CLOSE_DELAY=1000");
    config.setAsyncExecutorEnabled(true);
    config.setAsyncExecutorActivate(true);
    config.setAsyncExecutorThreadPoolQueueSize(queueSize);
    config.setAsyncExecutorDefaultAsyncJobAcquireWaitTime(500);
    
    config.setDatabaseSchemaUpdate("drop-create");
    config.setDataSource(dataSource);

    return config.buildProcessEngine();  
  }
  
}
