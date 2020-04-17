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
package org.activiti.engine.test.cfg.executioncount;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.ValidateExecutionRelatedEntityCountCfgCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.CountingExecutionEntity;
import org.activiti.engine.impl.persistence.entity.PropertyEntity;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class ChangeConfigAndRebootEngineTest extends ResourceActivitiTestCase {

  private static final Logger logger = LoggerFactory.getLogger(ChangeConfigAndRebootEngineTest.class);

  protected boolean newExecutionRelationshipCountValue;

  public ChangeConfigAndRebootEngineTest() {
    // Simply boot up the same engine with the usual config file
    // This way, database tests work. the only thing we have to make
    // sure is to give the process engine a name so it is
    // registered and unregistered separately.
    super("activiti.cfg.xml", ChangeConfigAndRebootEngineTest.class.getName());
  }

  @Override
  protected void additionalConfiguration(ProcessEngineConfiguration processEngineConfiguration) {
    logger.info("Applying additional config: setting schema update to true and enabling execution relationship count");
    processEngineConfiguration.setDatabaseSchemaUpdate("true");
    ((ProcessEngineConfigurationImpl) processEngineConfiguration).setEnableExecutionRelationshipCounts(newExecutionRelationshipCountValue);
  }

  protected void rebootEngine(boolean newExecutionRelationshipCountValue) {
    logger.info("Rebooting engine");
    this.newExecutionRelationshipCountValue = newExecutionRelationshipCountValue;
    closeDownProcessEngine();
    initializeProcessEngine();
    initializeServices();
  }

  @Deployment
  public void testChangeExecutionCountSettingAndRebootengine() {

    // Reboot, making sure the setting is applied
    rebootEngine(true);
    assertConfigProperty(true);

    // Start a process instance. All executions should have a count enabled flag set
    // and a task count of 1 for the child execution
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");
    assertExecutions(processInstance, true);

    // Reboot with same settings. Nothing should have changed
    rebootEngine(true);
    assertConfigProperty(true);
    assertExecutions(processInstance, true);

    // Reboot by disabling the property now. All the executions their flag should have been removed
    rebootEngine(false);
    assertConfigProperty(false);
    assertExecutions(processInstance, false);

    // See if we can finish the process
    finishProcessInstance(processInstance);

    // False to false should do nothing
    rebootEngine(false);
    assertConfigProperty(false);

    // Start a new process
    processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");
    assertExecutions(processInstance, false);

    // Reboot, enabling the config property. however, the executions won't get the flag now
    rebootEngine(true);
    assertConfigProperty(true);
    assertExecutions(processInstance, false);

    // But the process can be finished
    finishProcessInstance(processInstance);
    processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");
    assertExecutions(processInstance, true);
    finishProcessInstance(processInstance);
  }

  protected void assertConfigProperty(boolean expectedValue) {
    PropertyEntity propertyEntity = managementService.executeCommand(new Command<PropertyEntity>() {
      @Override
      public PropertyEntity execute(CommandContext commandContext) {
        return commandContext.getPropertyEntityManager().findById(
            ValidateExecutionRelatedEntityCountCfgCmd.PROPERTY_EXECUTION_RELATED_ENTITY_COUNT);
      }
    });
    assertThat(Boolean.parseBoolean(propertyEntity.getValue())).isEqualTo(expectedValue);
  }

  protected void assertExecutions(ProcessInstance processInstance, boolean expectedCountIsEnabledFlag) {
    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertThat(executions).hasSize(2);
    for (Execution execution : executions) {
      CountingExecutionEntity countingExecutionEntity = (CountingExecutionEntity) execution;
      assertThat(countingExecutionEntity.isCountEnabled()).isEqualTo(expectedCountIsEnabledFlag);

      if (expectedCountIsEnabledFlag && execution.getParentId() != null) {
        assertThat(countingExecutionEntity.getTaskCount()).isEqualTo(1);
      }
    }
  }

  protected void finishProcessInstance(ProcessInstance processInstance) {
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    assertProcessEnded(processInstance.getId());
  }

}
