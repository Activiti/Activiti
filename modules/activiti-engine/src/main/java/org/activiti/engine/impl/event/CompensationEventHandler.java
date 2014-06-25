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

package org.activiti.engine.impl.event;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;


/**
 * @author Daniel Meyer
 */
public class CompensationEventHandler implements EventHandler {
  
  public final static String EVENT_HANDLER_TYPE = "compensate";

  public String getEventHandlerType() {
    return EVENT_HANDLER_TYPE;
  }

  public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, CommandContext commandContext) {
        
    String configuration = eventSubscription.getConfiguration();
    if(configuration == null) {
      throw new ActivitiException("Compensating execution not set for compensate event subscription with id "+eventSubscription.getId());      
    }
    
    ExecutionEntity compensatingExecution = commandContext.getExecutionEntityManager()
            .findExecutionById(configuration);
   
    ActivityImpl compensationHandler = eventSubscription.getActivity();
    
    if((compensationHandler.getProperty(BpmnParse.PROPERTYNAME_IS_FOR_COMPENSATION) == null 
        ||!(Boolean)compensationHandler.getProperty(BpmnParse.PROPERTYNAME_IS_FOR_COMPENSATION))
            && compensationHandler.isScope()) {      
   
      // descend into scope:
      List<CompensateEventSubscriptionEntity> eventsForThisScope = compensatingExecution.getCompensateEventSubscriptions();      
      ScopeUtil.throwCompensationEvent(eventsForThisScope, compensatingExecution, false);
                  
    } else {
      try {

      	if(commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      		commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
      				ActivitiEventBuilder.createActivityEvent(ActivitiEventType.ACTIVITY_COMPENSATE, 
      						compensationHandler.getId(), 
      						(String) compensationHandler.getProperty("name"),
      						compensatingExecution.getId(), 
      						compensatingExecution.getProcessInstanceId(), 
      						compensatingExecution.getProcessDefinitionId(),
      						(String) compensatingExecution.getActivity().getProperties().get("type"), 
      						compensatingExecution.getActivity().getActivityBehavior().getClass().getCanonicalName()));
      	}
        compensatingExecution.setActivity(compensationHandler);
        
        // executing the atomic operation makes sure activity start events are fired
        compensatingExecution.performOperation(AtomicOperation.ACTIVITY_START);
        
      }catch (Exception e) {
        throw new ActivitiException("Error while handling compensation event "+eventSubscription, e);
      }
            
    }    
  }

}
