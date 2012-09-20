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

package org.activiti.cdi;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.cdi.annotation.ProcessInstanceId;
import org.activiti.cdi.annotation.TaskId;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

/**
 * Allows to access executions and tasks of a managed process instance via
 * dependency injection. A process instance can be managed, using the
 * {@link BusinessProcess}-bean.
 * 
 * The producer methods provided by this class have been extracted from the
 * {@link BusinessProcess}-bean in order to allow for specializing it.
 * 
 * @author Falko Menge
 */
public class CurrentProcessInstance {

  @Inject
  private BusinessProcess businessProcess;

  /**
   * Returns the {@link ProcessInstance} currently associated or 'null'
   * 
   * @throws ActivitiCdiException
   *           if no {@link Execution} is associated. Use
   *           {@link BusinessProcess#isAssociated()} to check whether an
   *           association exists.
   */
  /* Makes the current ProcessInstance available for injection */
  @Produces
  @Named
  public ProcessInstance getProcessInstance() {
    return businessProcess.getProcessInstance();
  }

  /**
   * Returns the id of the currently associated process instance or 'null'
   */
  /* Makes the processId available for injection */
  @Produces
  @Named
  @ProcessInstanceId
  public String getProcessInstanceId() {
    return businessProcess.getProcessInstanceId();
  }

  /**
   * Returns the currently associated execution or 'null'
   */
  /* Makes the current Execution available for injection */
  @Produces
  @Named
  public Execution getExecution() {
    return businessProcess.getExecution();
  }

  /**
   * @see BusinessProcess#getExecution()
   */
  /* Makes the id of the current Execution available for injection */
  @Produces
  @Named
  public String getExecutionId() {
    return businessProcess.getExecutionId();
  }

  /**
   * Returns the currently associated {@link Task} or 'null'
   * 
   * @throws ActivitiCdiException
   *           if no {@link Task} is associated. Use
   *           {@link BusinessProcess#isTaskAssociated()} to check whether an
   *           association exists.
   */
  /* Makes the current Task available for injection */
  @Produces
  @Named
  public Task getTask() {
    return businessProcess.getTask();
  }

  /**
   * Returns the id of the task associated with the current conversation or
   * 'null'.
   */
  /* Makes the taskId available for injection */
  @Produces
  @Named
  @TaskId
  public String getTaskId() {
    return businessProcess.getTaskId();
  }

}
