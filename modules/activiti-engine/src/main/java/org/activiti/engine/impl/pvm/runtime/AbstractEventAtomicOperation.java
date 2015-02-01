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

import java.util.List;

import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.pvm.PvmException;
import org.activiti.engine.impl.pvm.process.ScopeImpl;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractEventAtomicOperation implements AtomicOperation {
  
  public boolean isAsync(InterpretableExecution execution) {
    return false;
  }

  public void execute(InterpretableExecution execution) {
    ScopeImpl scope = getScope(execution);
    List<ExecutionListener> exectionListeners = scope.getExecutionListeners(getEventName());
    int executionListenerIndex = execution.getExecutionListenerIndex();
    
    if (exectionListeners.size()>executionListenerIndex) {
      execution.setEventName(getEventName());
      execution.setEventSource(scope);
      ExecutionListener listener = exectionListeners.get(executionListenerIndex);
      try {
        listener.notify(execution);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new PvmException("couldn't execute event listener : "+e.getMessage(), e);
      }
      execution.setExecutionListenerIndex(executionListenerIndex+1);
      execution.performOperation(this);

    } else {
      execution.setExecutionListenerIndex(0);
      execution.setEventName(null);
      execution.setEventSource(null);
      
      eventNotificationsCompleted(execution);
    }
  }

  protected abstract ScopeImpl getScope(InterpretableExecution execution);
  protected abstract String getEventName();
  protected abstract void eventNotificationsCompleted(InterpretableExecution execution);
}
