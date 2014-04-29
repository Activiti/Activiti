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
package org.activiti.engine.impl.bpmn.helper;

import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.event.MessageEventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;

/**
 * An {@link ActivitiEventListener} that throws a message event when an event is
 * dispatched to it. Sends the message to the execution the event was fired from. If the execution
 * is not subscribed to a message, the process-instance is checked.
 * 
 * @author Frederik Heremans
 * 
 */
public class MessageThrowingEventListener extends BaseDelegateEventListener {

	protected String messageName;
	protected Class<?> entityClass;
	
	@Override
	public void onEvent(ActivitiEvent event) {
		if(isValidEvent(event)) {
		
			if (event.getProcessInstanceId() == null) {
				throw new ActivitiIllegalArgumentException(
				    "Cannot throw process-instance scoped message, since the dispatched event is not part of an ongoing process instance");
			}
	
			CommandContext commandContext = Context.getCommandContext();
			List<EventSubscriptionEntity> subscriptionEntities = commandContext.getEventSubscriptionEntityManager()
				    .findEventSubscriptionsByNameAndExecution(MessageEventHandler.EVENT_HANDLER_TYPE, messageName, event.getExecutionId());
	
			// Revert to messaging the process instance
			if(subscriptionEntities.isEmpty() && event.getProcessInstanceId() != null && !event.getExecutionId().equals(event.getProcessInstanceId())) {
				subscriptionEntities = commandContext.getEventSubscriptionEntityManager()
				    .findEventSubscriptionsByNameAndExecution(MessageEventHandler.EVENT_HANDLER_TYPE, messageName, event.getProcessInstanceId());
			}
			
			for (EventSubscriptionEntity signalEventSubscriptionEntity : subscriptionEntities) {
				signalEventSubscriptionEntity.eventReceived(null, false);
			}
		}
	}

	public void setMessageName(String messageName) {
	  this.messageName = messageName;
  }
	
	@Override
	public boolean isFailOnException() {
		return true;
	}
}
