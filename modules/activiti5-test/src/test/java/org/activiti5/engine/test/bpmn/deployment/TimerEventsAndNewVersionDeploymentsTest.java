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

package org.activiti5.engine.test.bpmn.deployment;

import org.activiti.engine.repository.DeploymentProperties;
import org.activiti.engine.runtime.Job;
import org.activiti5.engine.impl.test.PluggableActivitiTestCase;

/**
 * A test specifically written to test how events (start/boundary) are handled 
 * when deploying a new version of a process definition. 
 * 
 * @author Joram Barrez
 */
public class TimerEventsAndNewVersionDeploymentsTest extends PluggableActivitiTestCase {
  
  private static final String TEST_TIMER_PROCESS = 
      "org/activiti5/engine/test/bpmn/deployment/TimerEventsAndNewVersionDeploymentsTest.timerTest.bpmn20.xml";
  
  private static final String TEST_PROCESS_NO_TIMER =
      "org/activiti5/engine/test/bpmn/deployment/TimerEventsAndNewVersionDeploymentsTest.processWithoutEvents.bpmn20.xml";
  
  public void testTimerCreationOnNewDeployments() {
    String deploymentId1 = deployTimerProcess();
    assertTimerJobs(1);
    
    String deploymentId2 = deployTimerProcess();
    assertTimerJobs(1);
    
    String deploymentId3 = deployProcessWithoutTimers(); // Process has same key
    assertTimerJobs(0);
    
    String deploymentId4 = deployTimerProcess();
    assertTimerJobs(1);
    
    cleanup(deploymentId1, deploymentId2, deploymentId3, deploymentId4);
  }
  
  public void testTimerRestoreOnDeploymentDelete1() {
    String deploymentId1 = deployTimerProcess();
    String deploymentId2 = deployProcessWithoutTimers(); // Process has same key
    String deploymentId3 = deployTimerProcess();
    String deploymentId4 = deployProcessWithoutTimers();
    
    assertTimerJobs(0);
    
    repositoryService.deleteDeployment(deploymentId4, true);
    assertTimerJobs(1);
    Job job = managementService.createTimerJobQuery().singleResult();
    assertEquals(repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId3).singleResult().getId(), job.getProcessDefinitionId());
    
    cleanup(deploymentId1, deploymentId2, deploymentId3);
  }
  
  public void testTimerRestoreOnDeploymentDelete2() {
    String deploymentId1 = deployTimerProcess();
    String deploymentId2 = deployProcessWithoutTimers(); // Process has same key
    String deploymentId3 = deployTimerProcess();
    String deploymentId4 = deployProcessWithoutTimers();
    
    assertTimerJobs(0);
    
    repositoryService.deleteDeployment(deploymentId3, true);
    assertTimerJobs(0);
    repositoryService.deleteDeployment(deploymentId2, true);
    assertTimerJobs(0);
    repositoryService.deleteDeployment(deploymentId4, true);
    assertTimerJobs(1);
    Job job = managementService.createTimerJobQuery().singleResult();
    assertEquals(repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId1).singleResult().getId(), job.getProcessDefinitionId());
    
    cleanup(deploymentId1);
  }
  
  /*
   * HELPERS
   */
  
  private String deployTimerProcess() {
    return deploy(TEST_TIMER_PROCESS);
  }
  
  private String deployProcessWithoutTimers() {
    return deploy(TEST_PROCESS_NO_TIMER);
  }
  
  private String deploy(String path) {
    String deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource(path)
      .deploymentProperty(DeploymentProperties.DEPLOY_AS_ACTIVITI5_PROCESS_DEFINITION, Boolean.TRUE)
      .deploy()
      .getId();
    return deploymentId;
  }
  
  private void assertTimerJobs(long count) {
    assertEquals(count, managementService.createTimerJobQuery().count());
  }
  
  private void cleanup(String ... deploymentIds) {
    for (String deploymentId : deploymentIds) {
      repositoryService.deleteDeployment(deploymentId, true);
    }
  }
  
}
