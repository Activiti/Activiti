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

import java.util.logging.Logger;

import org.activiti.engine.impl.pvm.process.ActivityImpl;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationTransitionCreateScope implements AtomicOperation {
  
  private static Logger log = Logger.getLogger(AtomicOperationTransitionCreateScope.class.getName());

  public void execute(ExecutionImpl execution) {
    ExecutionImpl propagatingExecution = null;
    ActivityImpl activity = execution.getActivity();
    if (activity.isScope()) {
      propagatingExecution = (ExecutionImpl) execution.createExecution();
      propagatingExecution.setActivity(activity);
      propagatingExecution.setTransition(execution.getTransition());
      execution.setTransition(null);
      execution.setActivity(null);
      execution.setActive(false);
      log.fine("create scope: parent "+execution+" continues as execution "+propagatingExecution);
      propagatingExecution.initialize();

    } else {
      propagatingExecution = execution;
    }

    propagatingExecution.performOperation(AtomicOperation.TRANSITION_NOTIFY_LISTENER_START);
  }
}
