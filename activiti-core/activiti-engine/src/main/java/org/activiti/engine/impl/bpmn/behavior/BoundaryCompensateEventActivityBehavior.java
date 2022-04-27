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

package org.activiti.engine.impl.bpmn.behavior;

import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;


public class BoundaryCompensateEventActivityBehavior extends BoundaryEventActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected CompensateEventDefinition compensateEventDefinition;

  public BoundaryCompensateEventActivityBehavior(CompensateEventDefinition compensateEventDefinition, boolean interrupting) {
    super(interrupting);
    this.compensateEventDefinition = compensateEventDefinition;
  }

  @Override
  public void execute(DelegateExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    BoundaryEvent boundaryEvent = (BoundaryEvent) execution.getCurrentFlowElement();

    Process process = ProcessDefinitionUtil.getProcess(execution.getProcessDefinitionId());
    if (process == null) {
      throw new ActivitiException("Process model (id = " + execution.getId() + ") could not be found");
    }

    Activity compensationActivity = null;
    List<Association> associations = process.findAssociationsWithSourceRefRecursive(boundaryEvent.getId());
    for (Association association : associations) {
      FlowElement targetElement = process.getFlowElement(association.getTargetRef(), true);
      if (targetElement instanceof Activity) {
        Activity activity = (Activity) targetElement;
        if (activity.isForCompensation()) {
          compensationActivity = activity;
          break;
        }
      }
    }

    if (compensationActivity == null) {
      throw new ActivitiException("Compensation activity could not be found (or it is missing 'isForCompensation=\"true\"'");
    }

    // find SubProcess or Process instance execution
    ExecutionEntity scopeExecution = null;
    ExecutionEntity parentExecution = executionEntity.getParent();
    while (scopeExecution == null && parentExecution != null) {
      if (parentExecution.getCurrentFlowElement() instanceof SubProcess) {
        scopeExecution = parentExecution;

      } else if (parentExecution.isProcessInstanceType()) {
        scopeExecution = parentExecution;
      } else {
        parentExecution = parentExecution.getParent();
      }
    }

    if (scopeExecution == null) {
      throw new ActivitiException("Could not find a scope execution for compensation boundary event " + boundaryEvent.getId());
    }

    Context.getCommandContext().getEventSubscriptionEntityManager().insertCompensationEvent(
        scopeExecution, compensationActivity.getId());
  }

  @Override
  public void trigger(DelegateExecution execution, String triggerName, Object triggerData) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    BoundaryEvent boundaryEvent = (BoundaryEvent) execution.getCurrentFlowElement();

    if (boundaryEvent.isCancelActivity()) {
      EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
      List<EventSubscriptionEntity> eventSubscriptions = executionEntity.getEventSubscriptions();
      for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
        if (eventSubscription instanceof CompensateEventSubscriptionEntity && eventSubscription.getActivityId().equals(compensateEventDefinition.getActivityRef())) {
          eventSubscriptionEntityManager.delete(eventSubscription);
        }
      }
    }

    super.trigger(executionEntity, triggerName, triggerData);
  }
}
