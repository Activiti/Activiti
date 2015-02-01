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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.event.MessageEventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class MessageEventReceivedCmd extends NeedsActiveExecutionCmd<Void> {
  
  private static final long serialVersionUID = 1L;
  
  protected final Serializable payload;
  protected final String messageName;
  protected final boolean async;
  
  public MessageEventReceivedCmd(String messageName, String executionId, Map<String, Object> processVariables) {
    super(executionId);
    this.messageName = messageName;
    
    if (processVariables != null) {
    	if (processVariables instanceof Serializable){
    		this.payload = (Serializable) processVariables;
    	}
    	else{	
    		this.payload = new HashMap<String, Object>(processVariables);
    	}
    		
    }
    else{
    	this.payload = null;
    }
    this.async = false;
  }

  public MessageEventReceivedCmd(String messageName, String executionId, boolean async) {
    super(executionId);
    this.messageName = messageName;
    this.payload = null;
    this.async = async;
  }
  
  protected Void execute(CommandContext commandContext, ExecutionEntity execution) {
    if(messageName == null) {
      throw new ActivitiIllegalArgumentException("messageName cannot be null");
    }
    
    List<EventSubscriptionEntity> eventSubscriptions = commandContext.getEventSubscriptionEntityManager()
      .findEventSubscriptionsByNameAndExecution(MessageEventHandler.EVENT_HANDLER_TYPE, messageName, executionId);
    
    if(eventSubscriptions.isEmpty()) {
      throw new ActivitiException("Execution with id '"+executionId+"' does not have a subscription to a message event with name '"+messageName+"'");
    }
    
    // there can be only one:
    EventSubscriptionEntity eventSubscriptionEntity = eventSubscriptions.get(0);
    
    eventSubscriptionEntity.eventReceived(payload, async);
    
    return null;
  }

  
}
