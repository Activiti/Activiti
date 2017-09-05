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

package org.activiti.spring.test.taskListener;

import org.activiti.bpmn.model.Task;
import org.activiti.engine.delegate.TransactionDependentTaskListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**

 */
public class MyTransactionDependentTaskListener implements TransactionDependentTaskListener {

  protected List<CurrentTask> currentTasks = new ArrayList<CurrentTask>();

  @Override
  public void notify(String processInstanceId, String executionId, Task task, Map<String, Object> executionVariables, Map<String, Object> customPropertiesMap) {
    currentTasks.add(new CurrentTask(processInstanceId, executionId, task.getId(), task.getName(), executionVariables, customPropertiesMap));
  }

  public List<CurrentTask> getCurrentTasks() {
    return currentTasks;
  }

  public static class CurrentTask {
    private final String processInstanceId;
    private final String executionId;
    private final String taskId;
    private final String taskName;
    private final Map<String, Object> executionVariables;
    private final Map<String, Object> customPropertiesMap;

    public CurrentTask(String processInstanceId, String executionId, String taskId, String taskName, Map<String, Object> executionVariables, Map<String, Object> customPropertiesMap) {
      this.processInstanceId = processInstanceId;
      this.executionId = executionId;
      this.taskId = taskId;
      this.taskName = taskName;
      this.executionVariables = executionVariables;
      this.customPropertiesMap = customPropertiesMap;
    }

    public String getProcessInstanceId() {
      return processInstanceId;
    }

    public String getExecutionId() {
      return executionId;
    }

    public String getTaskId() {
      return taskId;
    }

    public String getTaskName() {
      return taskName;
    }

    public Map<String, Object> getExecutionVariables() {
      return executionVariables;
    }

    public Map<String, Object> getCustomPropertiesMap() {
      return customPropertiesMap;
    }
  }
}
