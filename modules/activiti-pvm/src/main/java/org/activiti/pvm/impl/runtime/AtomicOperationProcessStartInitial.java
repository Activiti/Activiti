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

package org.activiti.pvm.impl.runtime;

import java.util.List;

import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.process.ProcessDefinitionImpl;
import org.activiti.pvm.impl.process.ScopeImpl;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationProcessStartInitial extends AbstractEventAtomicOperation {

  @Override
  protected ScopeImpl getScope(ExecutionImpl execution) {
    return execution.getActivity();
  }

  @Override
  protected String getEventName() {
    return EventListener.EVENTNAME_START;
  }

  @Override
  protected void eventNotificationsCompleted(ExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();
    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    if (activity==processDefinition.getInitial()) {
      execution.performOperation(ACTIVITY_EXECUTE);

    } else {
      List<ActivityImpl> initialActivityStack = processDefinition.getInitialActivityStack();
      int index = initialActivityStack.indexOf(activity);
      activity = initialActivityStack.get(index+1);

      ExecutionImpl executionToUse = null;
      if (activity.isScope()) {
        executionToUse = execution.getExecutions().get(0);
      } else {
        executionToUse = execution;
      }
      executionToUse.setActivity(activity);
      executionToUse.performOperation(PROCESS_START_INITIAL);
    }
  }
}
