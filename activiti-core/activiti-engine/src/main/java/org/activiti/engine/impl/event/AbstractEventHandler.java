/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import java.util.Map;

import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


public abstract class AbstractEventHandler implements EventHandler {

  public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, CommandContext commandContext) {
    ExecutionEntity execution = eventSubscription.getExecution();
    FlowNode currentFlowElement = (FlowNode) execution.getCurrentFlowElement();

    if (currentFlowElement == null) {
      throw new ActivitiException("Error while sending signal for event subscription '" + eventSubscription.getId() + "': " + "no activity associated with event subscription");
    }

    if (payload instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> processVariables = (Map<String, Object>) payload;
      execution.setVariables(processVariables);
    }

    Context.getAgenda().planTriggerExecutionOperation(execution);
  }

}
