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

import org.activiti.pvm.activity.SubProcessActivityBehavior;
import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.impl.process.ScopeImpl;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationProcessEnd extends AbstractEventAtomicOperation {

  @Override
  protected ScopeImpl getScope(ExecutionImpl execution) {
    return execution.getProcessDefinition();
  }

  @Override
  protected String getEventName() {
    return EventListener.EVENTNAME_END;
  }

  @Override
  protected void eventNotificationsCompleted(ExecutionImpl execution) {
    execution.destroy();
    execution.remove();

    ExecutionImpl superExecution = execution.getSuperExecution();
    if (superExecution!=null) {
      superExecution.setSubProcessInstance(null);
      SubProcessActivityBehavior subProcessActivityBehavior = (SubProcessActivityBehavior) superExecution.getActivity().getActivityBehavior();
      try {
        subProcessActivityBehavior.completed(superExecution);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
