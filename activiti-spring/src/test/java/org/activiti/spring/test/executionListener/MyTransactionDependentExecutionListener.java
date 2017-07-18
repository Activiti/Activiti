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

package org.activiti.spring.test.executionListener;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.delegate.TransactionDependentExecutionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**

 */
public class MyTransactionDependentExecutionListener implements TransactionDependentExecutionListener {

  protected List<CurrentActivity> currentActivities = new ArrayList<CurrentActivity>();

  @Override
  public void notify(String processInstanceId, String executionId, FlowElement currentFlowElement, Map<String, Object> executionVariables, Map<String, Object> customPropertiesMap) {
    currentActivities.add(new CurrentActivity(processInstanceId, executionId, currentFlowElement.getId(), currentFlowElement.getName(), executionVariables, customPropertiesMap));
  }

  public List<CurrentActivity> getCurrentActivities() {
    return currentActivities;
  }

  public static class CurrentActivity {
    private final String processInstanceId;
    private final String executionId;
    private final String activityId;
    private final String activityName;
    private final Map<String, Object> executionVariables;
    private final Map<String, Object> customPropertiesMap;

    public CurrentActivity(String processInstanceId, String executionId, String activityId, String activityName, Map<String, Object> executionVariables, Map<String, Object> customPropertiesMap) {
      this.processInstanceId = processInstanceId;
      this.executionId = executionId;
      this.activityId = activityId;
      this.activityName = activityName;
      this.executionVariables = executionVariables;
      this.customPropertiesMap = customPropertiesMap;
    }

    public String getProcessInstanceId() {
      return processInstanceId;
    }

    public String getExecutionId() {
      return executionId;
    }

    public String getActivityId() {
      return activityId;
    }

    public String getActivityName() {
      return activityName;
    }

    public Map<String, Object> getExecutionVariables() {
      return executionVariables;
    }

    public Map<String, Object> getCustomPropertiesMap() {
      return customPropertiesMap;
    }
  }





}
