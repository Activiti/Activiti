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
package org.activiti.engine.impl.pvm.runtime;

import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationTransitionCreateScope implements AtomicOperation {
  
  private static Logger log = LoggerFactory.getLogger(AtomicOperationTransitionCreateScope.class);
  
  public boolean isAsync(InterpretableExecution execution) {
    ActivityImpl activity = (ActivityImpl) execution.getActivity();
    return activity.isAsync();
  }

  public void execute(InterpretableExecution execution) {
    InterpretableExecution propagatingExecution = null;
    ActivityImpl activity = (ActivityImpl) execution.getActivity();
    if (activity.isScope()) {
      propagatingExecution = (InterpretableExecution) execution.createExecution();
      propagatingExecution.setActivity(activity);
      propagatingExecution.setTransition(execution.getTransition());
      execution.setTransition(null);
      execution.setActivity(null);
      execution.setActive(false);
      log.debug("create scope: parent {} continues as execution {}", execution, propagatingExecution);
      propagatingExecution.initialize();

    } else {
      propagatingExecution = execution;
    }

    propagatingExecution.performOperation(AtomicOperation.TRANSITION_NOTIFY_LISTENER_START);
  }
}
