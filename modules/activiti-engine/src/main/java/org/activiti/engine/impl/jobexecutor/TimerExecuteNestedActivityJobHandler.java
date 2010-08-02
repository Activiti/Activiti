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
package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.runtime.ActivityInstanceEntity;
import org.activiti.engine.impl.persistence.runtime.ProcessInstanceEntity;
import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.runtime.ExecutionContextImpl;


/**
 * @author Tom Baeyens
 */
public class TimerExecuteNestedActivityJobHandler implements JobHandler {
  
  public static final String TYPE = "timer-transition";

  public String getType() {
    return TYPE;
  }

  public void execute(String configuration, ProcessInstanceEntity processInstance, ActivityInstanceEntity activityInstance, CommandContext commandContext) {
    ActivityImpl activity = activityInstance.getActivity();
    ActivityImpl borderEventActivity = activity.getProcessDefinition().findActivity(configuration);

    if (borderEventActivity == null) {
      throw new ActivitiException("Error while firing timer: activity " + configuration + " not found");
    }
    
    new ExecutionContextImpl()
      .executeTimerNestedActivity(borderEventActivity);
  }
}
