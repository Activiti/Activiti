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

package org.activiti.examples.bpmn.executionlistener;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * Simple {@link ExecutionListener} that sets the current activity id and name attributes on the execution.
 * 
 * @author Tijs Rademakers
 */
public class CurrentActivityExecutionListener implements ExecutionListener {

  private static List<CurrentActivity> currentActivities = new ArrayList<CurrentActivity>();

  public static class CurrentActivity {
    private final String activityId;
    private final String activityName;
    
    public CurrentActivity(String activityId, String activityName) {
      this.activityId = activityId;
      this.activityName = activityName;
    }

    public String getActivityId() {
      return activityId;
    }
    
    public String getActivityName() {
      return activityName;
    }
  }
  
  public void notify(DelegateExecution execution) throws Exception {
    currentActivities.add(new CurrentActivity(execution.getCurrentActivityId(), execution.getCurrentActivityName()));
  }

  public static List<CurrentActivity> getCurrentActivities() {
    return currentActivities;
  }
  
  public static void clear() {
    currentActivities.clear();
  }
}
