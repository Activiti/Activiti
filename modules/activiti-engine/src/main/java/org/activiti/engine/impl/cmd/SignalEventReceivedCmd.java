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
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;


/**
 * @author Daniel Meyer
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
       signalEvents = commandContext.getEventSubscriptionManager()
        .findSignalEventSubscriptionsByEventName(eventName);              
    } else {
      signalEvents = commandContext.getEventSubscriptionManager()
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
      signalEventSubscriptionEntity.eventReceived(payload, false);        
    }
    
    return null;
  }

}
