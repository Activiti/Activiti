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
import org.activiti.engine.impl.event.MessageEventHandler;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;


/**
 * @author Daniel Meyer
 */
public class MessageEventReceivedCmd implements Command<Void> {
  
  protected final Map<String, Object> processVariables;
  protected final String messageName;
  protected final String executionId;

  public MessageEventReceivedCmd(String messageName, String executionId, Map<String, Object> processVariables) {
    this.messageName = messageName;
    this.executionId = executionId;
    this.processVariables = processVariables;
  }

  public Void execute(CommandContext commandContext) {
    if(messageName == null) {
      throw new ActivitiException("messageName cannot be null");
    }
    if(executionId == null) {
      throw new ActivitiException("executionId cannot be null");
    }
    
    List<EventSubscriptionEntity> eventSubscriptions = commandContext.getEventSubscriptionManager()
      .findEventSubscriptionsByNameAndExecution(MessageEventHandler.EVENT_HANDLER_TYPE, messageName, executionId);
    
    if(eventSubscriptions.isEmpty()) {
      throw new ActivitiException("Execution with id '"+executionId+"' does not have a subscription to a message event with name '"+messageName+"'");
    }
    
    // there can be only one:
    EventSubscriptionEntity eventSubscriptionEntity = eventSubscriptions.get(0);
    
    HashMap<String, Object> payload = null;
    if(processVariables != null) {
      payload = new HashMap<String, Object>(processVariables);
    }
    
    eventSubscriptionEntity.eventReceived(payload, false);
    
    return null;
  }

  
}
