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
package org.activiti.engine.test.bpmn.event.timer;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.activiti.engine.impl.cmd.CancelJobsCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class StartTimerEventTest extends PluggableActivitiTestCase {

  @Deployment
  public void testDurationStartTimerEvent() throws Exception {

    // Set the clock fixed
    Date startTime = new Date();

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    // After setting the clock to time '50minutes and 5 seconds', the second timer should fire
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((50 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L, 25L);

    List<ProcessInstance> pi = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample")
        .list();
    assertEquals(1, pi.size());

    assertEquals(0, jobQuery.count());


  }


  @Deployment
  public void testFixedDateStartTimerEvent() throws Exception {
    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    processEngineConfiguration.getClock().setCurrentTime(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("15/11/2036 11:12:30"));
    waitForJobExecutorToProcessAllJobs(5000L, 25L);

    List<ProcessInstance> pi = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample").list();
    assertEquals(1, pi.size());

    assertEquals(0, jobQuery.count());
  }

  // FIXME: This test likes to run in an endless loop when invoking the waitForJobExecutorOnCondition method
  @Deployment
  public void testCycleDateStartTimerEvent() throws Exception {
    processEngineConfiguration.getClock().setCurrentTime(new Date());

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();    
    assertEquals(1, jobQuery.count());
    
    final ProcessInstanceQuery piq = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample");
    
    moveByMinutes(5);
    waitForJobExecutorOnCondition(10000, 500, new Callable<Boolean>() {
      public Boolean call() throws Exception {
        return 1 == piq.count();
      }      
    });
    
    assertEquals(1, jobQuery.count());

    moveByMinutes(5);
    waitForJobExecutorOnCondition(10000, 500, new Callable<Boolean>() {
      public Boolean call() throws Exception {
        return 2 ==  piq.count();
      }      
    });
    
    assertEquals(1, jobQuery.count());
    //have to manually delete pending timer
    cleanDB();
  }

  
  private void moveByMinutes(int minutes) throws Exception {
    processEngineConfiguration.getClock().setCurrentTime(new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() + ((minutes * 60 * 1000) + 5000)));
  }

  @Deployment
  public void testCycleWithLimitStartTimerEvent() throws Exception {
    processEngineConfiguration.getClock().setCurrentTime(new Date());

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());
    
    moveByMinutes(6);
    managementService.executeJob(managementService.createJobQuery().singleResult().getId());
    assertEquals(1, jobQuery.count());

    moveByMinutes(6);
    managementService.executeJob(managementService.createJobQuery().singleResult().getId());
    assertEquals(0, jobQuery.count());

  }
  
  @Deployment
  public void testExpressionStartTimerEvent() throws Exception {
    // ACT-1415: fixed start-date is an expression
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    processEngineConfiguration.getClock().setCurrentTime(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("15/11/2036 11:12:30"));
    waitForJobExecutorToProcessAllJobs(5000L, 25L);

    List<ProcessInstance> pi = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample")
        .list();
    assertEquals(1, pi.size());

    assertEquals(0, jobQuery.count());
  }
  
  @Deployment
  public void testVersionUpgradeShouldCancelJobs() throws Exception {
    processEngineConfiguration.getClock().setCurrentTime(new Date());

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    //we deploy new process version, with some small change
    String process = new String(IoUtil.readInputStream(getClass().getResourceAsStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml"), "")).replaceAll("beforeChange","changed");
    String id = repositoryService.createDeployment().addInputStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml",
        new ByteArrayInputStream(process.getBytes())).deploy().getId();

    assertEquals(1, jobQuery.count());

    moveByMinutes(5);
    waitForJobExecutorOnCondition(10000, 500, new Callable<Boolean>() {
      public Boolean call() throws Exception {
        //we check that correct version was started
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample").singleResult();
        if(processInstance != null) {
          String pi = processInstance.getProcessInstanceId();        
          return "changed".equals(runtimeService.getActiveActivityIds(pi).get(0));
        }else {
          return false;
        }
      }      
    });
    assertEquals(1, jobQuery.count());

    cleanDB();
    repositoryService.deleteDeployment(id, true);
  }
  
  @Deployment
  public void testTimerShouldNotBeRecreatedOnDeploymentCacheReboot() {
    
    // Just to be sure, I added this test. Sounds like something that could easily happen
    // when the order of deploy/parsing is altered.
    
    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());
    
    // Reset deployment cache
    processEngineConfiguration.getProcessDefinitionCache().clear();
    
    // Start one instance of the process definition, this will trigger a cache reload
    runtimeService.startProcessInstanceByKey("startTimer");
    
    // No new jobs should have been created
    assertEquals(1, jobQuery.count());
  }
  
  // Test for ACT-1533
  public void testTimerShouldNotBeRemovedWhenUndeployingOldVersion() throws Exception {
    // Deploy test process
    String processXml = new String(IoUtil.readInputStream(getClass().getResourceAsStream("StartTimerEventTest.testTimerShouldNotBeRemovedWhenUndeployingOldVersion.bpmn20.xml"), ""));
    String firstDeploymentId = repositoryService.createDeployment().addInputStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml",
            new ByteArrayInputStream(processXml.getBytes())).deploy().getId();
    
    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    //we deploy new process version, with some small change
    String processChanged = processXml.replaceAll("beforeChange","changed");
    String secondDeploymentId = repositoryService.createDeployment().addInputStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml",
        new ByteArrayInputStream(processChanged.getBytes())).deploy().getId();
    assertEquals(1, jobQuery.count());

    // Remove the first deployment
    repositoryService.deleteDeployment(firstDeploymentId, true);
    
    // The removal of an old version should not affect timer deletion
    // ACT-1533: this was a bug, and the timer was deleted!
    assertEquals(1, jobQuery.count());
    
    // Cleanup
    cleanDB();
    repositoryService.deleteDeployment(secondDeploymentId, true);
  }
  
  public void testOldJobsDeletedOnRedeploy() {
  	
  	for (int i=0; i<3; i++) {
  	
	  	repositoryService.createDeployment()
	  		.addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testOldJobsDeletedOnRedeploy.bpmn20.xml")
	  		.deploy();
  	
	  	assertEquals(i+1, repositoryService.createDeploymentQuery().count());
	  	assertEquals(i+1, repositoryService.createProcessDefinitionQuery().count());
	  	assertEquals(1, managementService.createJobQuery().count());

  	}
  	
  	// Cleanup
  	for (ProcessDefinition processDefinition : repositoryService.createProcessDefinitionQuery().processDefinitionKey("timer").orderByProcessDefinitionVersion().desc().list()) {
  		repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
  	}
  	
  	assertEquals(0, managementService.createJobQuery().count());
  	
  }

  public void testTimersRecreatedOnDeploymentDelete() {
  	
  	// v1 has timer
  	// v2 has no timer
  	// v3 has no timer
  	// v4 has no timer
  	
  	// Deploy v1
  	String deployment1 = repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v1.bpmn20.xml")
			.deploy().getId();
  	
  	assertEquals(1, repositoryService.createDeploymentQuery().count());
  	assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
  	assertEquals(1, managementService.createJobQuery().count());
  	
  	// Deploy v2: no timer -> previous should be deleted
  	String deployment2 = repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v2.bpmn20.xml")
			.deploy().getId();
	
  	assertEquals(2, repositoryService.createDeploymentQuery().count());
  	assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
  	assertEquals(0, managementService.createJobQuery().count());
  	
    // Deploy v3: no timer 
  	String deployment3 = repositoryService.createDeployment()
 			.addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v3.bpmn20.xml")
 			.deploy().getId();
 	
   	assertEquals(3, repositoryService.createDeploymentQuery().count());
   	assertEquals(3, repositoryService.createProcessDefinitionQuery().count());
   	assertEquals(0, managementService.createJobQuery().count());
   	
    // Deploy v4: no timer 
   	String deployment4 = repositoryService.createDeployment()
 			.addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v4.bpmn20.xml")
 			.deploy().getId();
 	
   	assertEquals(4, repositoryService.createDeploymentQuery().count());
   	assertEquals(4, repositoryService.createProcessDefinitionQuery().count());
   	assertEquals(1, managementService.createJobQuery().count());
   	
   	// Delete v4 -> V3 active. No timer active anymore (v3 doesn't have a timer)
   	repositoryService.deleteDeployment(deployment4, true);
  	assertEquals(3, repositoryService.createDeploymentQuery().count());
   	assertEquals(3, repositoryService.createProcessDefinitionQuery().count());
   	assertEquals(0, managementService.createJobQuery().count());
   	
  	// Delete v2 --> V3 still active, nothing changed there
   	repositoryService.deleteDeployment(deployment2, true);
  	assertEquals(2, repositoryService.createDeploymentQuery().count());
   	assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
   	assertEquals(0, managementService.createJobQuery().count()); // v3 is still active
   	
   	// Delete v3 -> fallback to v1
  	repositoryService.deleteDeployment(deployment3, true);
  	assertEquals(1, repositoryService.createDeploymentQuery().count());
   	assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
   	assertEquals(1, managementService.createJobQuery().count());
  	
  	// Cleanup
  	for (ProcessDefinition processDefinition : repositoryService.createProcessDefinitionQuery().processDefinitionKey("timer").orderByProcessDefinitionVersion().desc().list()) {
  		repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
  	}
  	
  	assertEquals(0, managementService.createJobQuery().count());
  	
  }
  
  // Same test as above, but now with tenants
  public void testTimersRecreatedOnDeploymentDeleteWithTenantId() {

  	// Deploy 4 versions without tenantId
  	for (int i=1; i<=4; i++) {
  		repositoryService.createDeployment()
				.addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v" + i + ".bpmn20.xml")
				.deploy();
  	}
  	
  	String testTenant = "Activiti-tenant";
  	
  	// Deploy v1
  	String deployment1 = repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v1.bpmn20.xml")
			.tenantId(testTenant)
			.deploy().getId();
  	
  	assertEquals(1, repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count());
  	assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count());
  	assertEquals(1, managementService.createJobQuery().jobTenantId(testTenant).count());
  	
  	// Deploy v2: no timer -> previous should be deleted
  	String deployment2 = repositoryService.createDeployment()
			.addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v2.bpmn20.xml")
			.tenantId(testTenant)
			.deploy().getId();
  	
  	assertEquals(2, repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count());
  	assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count());
  	assertEquals(0, managementService.createJobQuery().jobTenantId(testTenant).count());
	
    // Deploy v3: no timer 
  	String deployment3 = repositoryService.createDeployment()
 			.addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v3.bpmn20.xml")
 			.tenantId(testTenant)
 			.deploy().getId();
  	
  	assertEquals(3, repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count());
  	assertEquals(3, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count());
  	assertEquals(0, managementService.createJobQuery().jobTenantId(testTenant).count());
 	
    // Deploy v4: no timer 
   	String deployment4 = repositoryService.createDeployment()
 			.addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v4.bpmn20.xml")
 			.tenantId(testTenant)
 			.deploy().getId();
   	
  	assertEquals(4, repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count());
  	assertEquals(4, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count());
  	assertEquals(1, managementService.createJobQuery().jobTenantId(testTenant).count());
 	
   	// Delete v4 -> V3 active. No timer active anymore (v3 doesn't have a timer)
   	repositoryService.deleteDeployment(deployment4, true);
  	assertEquals(3, repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count());
  	assertEquals(3, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count());
  	assertEquals(0, managementService.createJobQuery().jobTenantId(testTenant).count());
   	
  	// Delete v2 --> V3 still active, nothing changed there
   	repositoryService.deleteDeployment(deployment2, true);
  	assertEquals(2, repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count());
  	assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count());
  	assertEquals(0, managementService.createJobQuery().jobTenantId(testTenant).count());
   	
   	// Delete v3 -> fallback to v1
  	repositoryService.deleteDeployment(deployment3, true);
  	assertEquals(1, repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count());
  	assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count());
  	assertEquals(1, managementService.createJobQuery().jobTenantId(testTenant).count());
  	
  	// Cleanup
  	for (ProcessDefinition processDefinition : repositoryService.createProcessDefinitionQuery().processDefinitionKey("timer").orderByProcessDefinitionVersion().desc().list()) {
  		repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
  	}
  	
  	assertEquals(0, managementService.createJobQuery().count());
  	
  }
  
  private void cleanDB() {
    String jobId = managementService.createJobQuery().singleResult().getId();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    commandExecutor.execute(new CancelJobsCmd(jobId));
  }

}
