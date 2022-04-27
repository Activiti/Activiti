/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.impl.event;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

import java.util.List;


public class CompensationEventHandler implements EventHandler {

  public String getEventHandlerType() {
    return CompensateEventSubscriptionEntity.EVENT_TYPE;
  }

  public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, CommandContext commandContext) {

    String configuration = eventSubscription.getConfiguration();
    if (configuration == null) {
      throw new ActivitiException("Compensating execution not set for compensate event subscription with id " + eventSubscription.getId());
    }

    ExecutionEntity compensatingExecution = commandContext.getExecutionEntityManager().findById(configuration);

    String processDefinitionId = compensatingExecution.getProcessDefinitionId();
    Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
    if (process == null) {
      throw new ActivitiException("Cannot start process instance. Process model (id = " + processDefinitionId + ") could not be found");
    }

    FlowElement flowElement = process.getFlowElement(eventSubscription.getActivityId(), true);

    if (flowElement instanceof SubProcess && !((SubProcess) flowElement).isForCompensation()) {

      // descend into scope:
      compensatingExecution.setScope(true);
      List<CompensateEventSubscriptionEntity> eventsForThisScope = commandContext.getEventSubscriptionEntityManager().findCompensateEventSubscriptionsByExecutionId(compensatingExecution.getId());
      ScopeUtil.throwCompensationEvent(eventsForThisScope, compensatingExecution, false);

    } else {

      try {

        if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
          commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
                ActivitiEventBuilder.createActivityEvent(ActivitiEventType.ACTIVITY_COMPENSATE, flowElement.getId(), flowElement.getName(),
                    compensatingExecution.getId(), compensatingExecution.getProcessInstanceId(), compensatingExecution.getProcessDefinitionId(), flowElement));
        }
        compensatingExecution.setCurrentFlowElement(flowElement);
        Context.getAgenda().planContinueProcessInCompensation(compensatingExecution);

      } catch (Exception e) {
        throw new ActivitiException("Error while handling compensation event " + eventSubscription, e);
      }

    }
  }

}
