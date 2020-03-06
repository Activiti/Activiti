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

package org.activiti.engine.impl.bpmn.behavior;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.impl.bpmn.parser.factory.MessageExecutionContext;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;

/**
 * Implementation of the BPMN 2.0 event subprocess message start event.
 * 

 */
public class EventSubProcessMessageStartEventActivityBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = 1L;
  
  protected final MessageEventDefinition messageEventDefinition;
  protected final MessageExecutionContext messageExecutionContext;
  
  public EventSubProcessMessageStartEventActivityBehavior(MessageEventDefinition messageEventDefinition,
                                                          MessageExecutionContext messageExecutionContext) {
    this.messageEventDefinition = messageEventDefinition;
    this.messageExecutionContext = messageExecutionContext;
  }

  public void execute(DelegateExecution execution) {
    StartEvent startEvent = (StartEvent) execution.getCurrentFlowElement();
    EventSubProcess eventSubProcess = (EventSubProcess) startEvent.getSubProcess();

    execution.setScope(true);

    // initialize the template-defined data objects as variables
    Map<String, Object> dataObjectVars = processDataObjects(eventSubProcess.getDataObjects());
    if (dataObjectVars != null) {
      execution.setVariablesLocal(dataObjectVars);
    }
  }
  
  @Override
  public void trigger(DelegateExecution execution, String triggerName, Object triggerData) {
    CommandContext commandContext = Context.getCommandContext();
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    
    StartEvent startEvent = (StartEvent) execution.getCurrentFlowElement();
    if (startEvent.isInterrupting()) {  
      List<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(executionEntity.getParentId());
      for (ExecutionEntity childExecution : childExecutions) {
        if (!childExecution.getId().equals(executionEntity.getId())) {
          executionEntityManager.cancelExecutionAndRelatedData(childExecution,
              DeleteReason.EVENT_SUBPROCESS_INTERRUPTING + "(" + startEvent.getId() + ")");
        }
      }
    }

    // Should we use triggerName and triggerData, because message name expression can change?
    String messageName = messageExecutionContext.getMessageName(execution);
    
    EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
    List<EventSubscriptionEntity> eventSubscriptions = executionEntity.getEventSubscriptions();
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      if (eventSubscription instanceof MessageEventSubscriptionEntity && eventSubscription.getEventName().equals(messageName)) {

        eventSubscriptionEntityManager.delete(eventSubscription);
      }
    }
    
    executionEntity.setCurrentFlowElement((SubProcess) executionEntity.getCurrentFlowElement().getParentContainer());
    executionEntity.setScope(true);
    
    ExecutionEntity outgoingFlowExecution = executionEntityManager.createChildExecution(executionEntity);
    outgoingFlowExecution.setCurrentFlowElement(startEvent);
    
    leave(outgoingFlowExecution);
  }

  protected Map<String, Object> processDataObjects(Collection<ValuedDataObject> dataObjects) {
    Map<String, Object> variablesMap = new HashMap<>();
    // convert data objects to process variables
    if (dataObjects != null) {
      for (ValuedDataObject dataObject : dataObjects) {
        variablesMap.put(dataObject.getName(), dataObject.getValue());
      }
    }
    return variablesMap;
  }
}
