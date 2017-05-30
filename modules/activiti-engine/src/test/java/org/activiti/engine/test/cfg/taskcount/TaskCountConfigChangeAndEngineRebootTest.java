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

package org.activiti.engine.test.cfg.taskcount;

import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.ValidateTaskRelatedEntityCountCfgCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.CountingTaskEntity;
import org.activiti.engine.impl.persistence.entity.PropertyEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskCountConfigChangeAndEngineRebootTest extends ResourceActivitiTestCase {

  private static final Logger logger = LoggerFactory.getLogger(TaskCountConfigChangeAndEngineRebootTest.class);

  protected boolean newTaskRelationshipCountValue;
	  
  public TaskCountConfigChangeAndEngineRebootTest() {

    // Simply boot up the same engine with the usual config file
    // This way, database tests work. the only thing we have to make
    // sure is to give the process engine a name so it is
    // registered and unregistered separately.
    super("activiti.cfg.xml", TaskCountConfigChangeAndEngineRebootTest.class.getName());
  }
	  
  @Override
  protected void additionalConfiguration(ProcessEngineConfiguration processEngineConfiguration) {
    logger.info("Applying additional config: setting schema update to true and enabling task relationship count");
	processEngineConfiguration.setDatabaseSchemaUpdate("true");
	  ((ProcessEngineConfigurationImpl) processEngineConfiguration).setEnableTaskRelationshipCounts(newTaskRelationshipCountValue);
	}

  protected void rebootEngine(boolean newTaskRelationshipCountValue) {
	logger.info("Rebooting engine");
	this.newTaskRelationshipCountValue = newTaskRelationshipCountValue;
	closeDownProcessEngine();
	initializeProcessEngine();
	initializeServices();
  }
  
  @Deployment
  public void testTaskCountSettingChangeAndEngineReboot() {
	    
    rebootFlagNotChanged(true);
    rebootFlagNotChanged(false);
    checkEnableFlagBetweenTasks();
    checkDisableFlagBetweenTasks();
	    
  }

  private void checkEnableFlagBetweenTasks() {
    rebootEngine(false);
    assertConfigProperty(false);
	    
    // Start a new process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");
    Task userTask1 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertTaskCountFlag(userTask1, false);
    // Reboot, enabling the config property. however, the task won't get the flag now
    rebootEngine(true);
    assertConfigProperty(true);
	    
    //re-fetch the task
    userTask1 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertTaskCountFlag(userTask1, false);
	    
    //complete the userTask1 and move to the next one
    taskService.complete(userTask1.getId());
	    
    //userTask2 created with the new flag (true)
    Task userTask2 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertTaskCountFlag(userTask2, true);
	    
    finishProcessInstance(processInstance);
  }
	  
  private void checkDisableFlagBetweenTasks() {
    rebootEngine(true);
    assertConfigProperty(true);
    
    // Start a new process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");
    Task userTask1 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertTaskCountFlag(userTask1, true);
    // Reboot, disabling the config property. The existing task will have the flag updated. 
    rebootEngine(false);
    assertConfigProperty(false);
    
    userTask1 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertTaskCountFlag(userTask1, false);
    
    //complete the userTask1 and move to the next one
    taskService.complete(userTask1.getId());
    
    //userTask2 created with flag false
    Task userTask2 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertTaskCountFlag(userTask2, false);
    
    finishProcessInstance(processInstance);
  }

  private void rebootFlagNotChanged(boolean enableTaskCountFlag) {
    rebootEngine(enableTaskCountFlag);
    assertConfigProperty(enableTaskCountFlag);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");
    Task userTask1 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertTaskCountFlag(userTask1, enableTaskCountFlag);
    
    // Reboot with same settings. Nothing should have changed
    rebootEngine(enableTaskCountFlag);
    assertConfigProperty(enableTaskCountFlag);
    assertTaskCountFlag(userTask1, enableTaskCountFlag);
    
    // userTask2 should have the same count flag
    taskService.complete(userTask1.getId());
    Task userTask2 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertTaskCountFlag(userTask2, enableTaskCountFlag);
    
    // See if we can finish the process
    finishProcessInstance(processInstance);
  }
 
  protected void assertConfigProperty(boolean expectedValue) {
    PropertyEntity propertyEntity = managementService.executeCommand(new Command<PropertyEntity>() {
    @Override
    public PropertyEntity execute(CommandContext commandContext) {
        return commandContext.getPropertyEntityManager().findById(
	            ValidateTaskRelatedEntityCountCfgCmd.PROPERTY_TASK_RELATED_ENTITY_COUNT);
      }
    });
    assertEquals(expectedValue, Boolean.parseBoolean(propertyEntity.getValue()));
  }
	  
  protected void assertTaskCountFlag(Task task, boolean enableTaskCountFlag){
    assertEquals(((CountingTaskEntity)task).isCountEnabled(), enableTaskCountFlag);
  }
  
  protected void finishProcessInstance(ProcessInstance processInstance) {
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    assertProcessEnded(processInstance.getId());
  }
}