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

package org.activiti.engine.impl.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.runtime.Execution;


/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class SignalEventReceivedCmd implements Command<Void> {
    
  protected final String eventName;
  protected final String executionId;
  protected final Map<String, Object> variables;

  public SignalEventReceivedCmd(String eventName, String executionId, Map<String, Object> variables) {
    this.eventName = eventName;
    this.executionId = executionId;
    this.variables = variables;
  }

  public Void execute(CommandContext commandContext) {
    
    List<SignalEventSubscriptionEntity> signalEvents = null;
    
    if(executionId == null) {
       signalEvents = commandContext.getEventSubscriptionEntityManager()
        .findSignalEventSubscriptionsByEventName(eventName);              
    } else {
      
      ExecutionEntity execution = commandContext.getExecutionEntityManager().findExecutionById(executionId);
      
      if (execution == null) {
        throw new ActivitiObjectNotFoundException("Cannot find execution with id '" + executionId + "'", Execution.class);
      }
      
      if (execution.isSuspended()) {
        throw new ActivitiException("Cannot throw signal event '" + eventName 
                + "' because execution '" + executionId + "' is suspended");
      }
      
      signalEvents = commandContext.getEventSubscriptionEntityManager()
        .findSignalEventSubscriptionsByNameAndExecution(eventName, executionId);
      
      if(signalEvents.isEmpty()) {
        throw new ActivitiException("Execution '"+executionId+"' has not subscribed to a signal event with name '"+eventName+"'.");      
      }
    }
        
    HashMap<String, Object> payload = null;
    if(variables != null) {
      payload = new HashMap<String, Object>(variables);
    }
    for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : signalEvents) {
      // We only throw the event to globally scoped signals. 
      // Process instance scoped signals must be thrown within the process itself 
      if (signalEventSubscriptionEntity.isGlobalScoped()) {
        signalEventSubscriptionEntity.eventReceived(payload, false);
      }
    }
    
    return null;
  }

}
