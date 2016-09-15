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

package org.activiti.engine.test.cfg.multitenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.asyncexecutor.multitenant.ExecutorPerTenantAsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.multitenant.SharedExecutorServiceAsyncExecutor;
import org.activiti.engine.impl.cfg.multitenant.MultiSchemaMultiTenantProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class MultiTenantProcessEngineTest {
  
  private DummyTenantInfoHolder tenantInfoHolder;
  private MultiSchemaMultiTenantProcessEngineConfiguration config;
  private ProcessEngine processEngine;
  
  @Before
  public void setup() {
    setupTenantInfoHolder();
  }
  
  @After
  public void close() {
    processEngine.close();
  }

  private void setupTenantInfoHolder() {
    DummyTenantInfoHolder tenantInfoHolder = new DummyTenantInfoHolder();
    
    tenantInfoHolder.addTenant("alfresco");
    tenantInfoHolder.addUser("alfresco", "joram");
    tenantInfoHolder.addUser("alfresco", "tijs");
    tenantInfoHolder.addUser("alfresco", "paul");
    tenantInfoHolder.addUser("alfresco", "yvo");
    
    tenantInfoHolder.addTenant("acme");
    tenantInfoHolder.addUser("acme", "raphael");
    tenantInfoHolder.addUser("acme", "john");
    
    tenantInfoHolder.addTenant("starkindustries");
    tenantInfoHolder.addUser("starkindustries", "tony");
    
    this.tenantInfoHolder = tenantInfoHolder;
  }
  
  private void setupProcessEngine(boolean sharedExecutor) {
    config = new MultiSchemaMultiTenantProcessEngineConfiguration(tenantInfoHolder);

    config.setDatabaseType(MultiSchemaMultiTenantProcessEngineConfiguration.DATABASE_TYPE_H2);
    config.setDatabaseSchemaUpdate(MultiSchemaMultiTenantProcessEngineConfiguration.DB_SCHEMA_UPDATE_DROP_CREATE);
    
    config.setAsyncExecutorEnabled(true);
    config.setAsyncExecutorActivate(true);
    
    
    if (sharedExecutor) {
      config.setAsyncExecutor(new SharedExecutorServiceAsyncExecutor(tenantInfoHolder));
    } else {
      config.setAsyncExecutor(new ExecutorPerTenantAsyncExecutor(tenantInfoHolder));
    }
    
    config.registerTenant("alfresco", createDataSource("jdbc:h2:mem:activiti-mt-alfresco;DB_CLOSE_DELAY=1000", "sa", ""));
    config.registerTenant("acme", createDataSource("jdbc:h2:mem:activiti-mt-acme;DB_CLOSE_DELAY=1000", "sa", ""));
    config.registerTenant("starkindustries", createDataSource("jdbc:h2:mem:activiti-mt-stark;DB_CLOSE_DELAY=1000", "sa", ""));
    
    
    processEngine = config.buildProcessEngine();
  }
  
  @Test
  public void testStartProcessInstancesWithSharedExecutor() throws Exception {
    setupProcessEngine(true);
    runProcessInstanceTest();
  }
  
  @Test
  public void testStartProcessInstancesWithExecutorPerTenantAsyncExecutor() throws Exception {
    setupProcessEngine(false);
    runProcessInstanceTest();
  }

  private void runProcessInstanceTest() throws InterruptedException {
    // Generate data
    startProcessInstances("joram");
    startProcessInstances("joram");
    startProcessInstances("joram");
    startProcessInstances("raphael");
    startProcessInstances("raphael");
    completeTasks("raphael");
    startProcessInstances("tony");
    
    // Verify
    assertData("joram", 6, 3);
    assertData("raphael", 0, 0);
    assertData("tony", 2, 1);
    
    // Adding a new tenant
    tenantInfoHolder.addTenant("dailyplanet");
    tenantInfoHolder.addUser("dailyplanet", "louis");
    tenantInfoHolder.addUser("dailyplanet", "clark");
    
    config.registerTenant("dailyplanet", createDataSource("jdbc:h2:mem:activiti-mt-daily;DB_CLOSE_DELAY=1000", "sa", ""));
    
    // Start process instance for new tenant
    startProcessInstances("clark");
    startProcessInstances("clark");
    assertData("clark", 4, 2);
    
    // Move the clock 2 hours (jobs fire in one hour)
    config.getClock().setCurrentTime(new Date(config.getClock().getCurrentTime().getTime() + (2 * 60 * 60 * 1000)));
    Thread.sleep(15000L); // acquire time is 10 seconds, so 15 should be ok
    
    assertData("joram", 6, 0);
    assertData("raphael", 0, 0);
    assertData("tony", 2, 0);
    assertData("clark", 4, 0);
  }
  
  private void startProcessInstances(String userId) {
    
    System.out.println();
    System.out.println("Starting process instance for user " + userId);
    
    tenantInfoHolder.setCurrentUserId(userId);
    
    Deployment deployment = processEngine.getRepositoryService().createDeployment()
          .addClasspathResource("org/activiti/engine/test/cfg/multitenant/oneTaskProcess.bpmn20.xml")
          .addClasspathResource("org/activiti/engine/test/cfg/multitenant/jobTest.bpmn20.xml")
          .deploy();
    System.out.println("Process deployed! Deployment id is " + deployment.getId());
    
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("data", "Hello from " + userId);
    
    ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess", vars);
    List<Task> tasks = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).list();
    System.out.println("Got " + tasks.size() + " tasks");
    
    System.out.println("Got " + processEngine.getHistoryService().createHistoricProcessInstanceQuery().count() + " process instances in the system");
    
    // Start a process instance with a Job
    processEngine.getRuntimeService().startProcessInstanceByKey("jobTest");
    
    tenantInfoHolder.clearCurrentUserId();
    tenantInfoHolder.clearCurrentTenantId();
  }
  
  private void completeTasks(String userId) {
    tenantInfoHolder.setCurrentUserId(userId);
    
   for (Task task : processEngine.getTaskService().createTaskQuery().list()) {
     processEngine.getTaskService().complete(task.getId());
   }
    
    tenantInfoHolder.clearCurrentUserId();
    tenantInfoHolder.clearCurrentTenantId();
  }
  
  private void assertData(String userId, long nrOfActiveProcessInstances, long nrOfActiveJobs) {
    tenantInfoHolder.setCurrentUserId(userId);
    
    Assert.assertEquals(nrOfActiveProcessInstances, processEngine.getRuntimeService().createProcessInstanceQuery().count());
    Assert.assertEquals(nrOfActiveProcessInstances, processEngine.getHistoryService().createHistoricProcessInstanceQuery().unfinished().count());
    Assert.assertEquals(nrOfActiveJobs, processEngine.getManagementService().createJobQuery().count());
    
    tenantInfoHolder.clearCurrentUserId();
    tenantInfoHolder.clearCurrentTenantId();
  }
  
  // Helper //////////////////////////////////////////
  
  
  private DataSource createDataSource(String jdbcUrl, String jdbcUsername, String jdbcPassword) {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL(jdbcUrl);
    ds.setUser(jdbcUsername);
    ds.setPassword(jdbcPassword);
    return ds;
  }

}
