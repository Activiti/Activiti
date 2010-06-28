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
package org.activiti.impl.job;

import org.activiti.ActivitiException;
import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.interceptor.CommandContext;


/**
 * @author Tom Baeyens
 */
public class TimerExecuteNestedActivityJobHandler implements JobHandler {
  
  public static final String TYPE = "timer-transition";

  public String getType() {
    return TYPE;
  }

  public void execute(String configuration, ExecutionImpl execution, CommandContext commandContext) {
    ActivityImpl activity = execution.getActivity();
    ActivityImpl borderEventActivity = activity.getActivity(configuration);

    if (borderEventActivity == null) {
      throw new ActivitiException("Error while firing timer: activity " + configuration + " not found");
    }
    
    // TODO in case of concurrency inside the timed scope, more execution juggling needs to be implemented here. 
    
    execution.executeActivity(borderEventActivity);
  }
}
