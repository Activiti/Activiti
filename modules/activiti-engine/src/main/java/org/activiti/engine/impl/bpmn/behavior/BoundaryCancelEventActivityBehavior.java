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

import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;

/**
 * @author Tijs Rademakers
 */
public class BoundaryCancelEventActivityBehavior extends BoundaryEventActivityBehavior {

  private static final long serialVersionUID = 1L;

  @Override
  public void trigger(ActivityExecution execution, String triggerName, Object triggerData) {
    BoundaryEvent boundaryEvent = (BoundaryEvent) execution.getCurrentFlowElement();
    ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
    
    ExecutionEntity subProcessExecution = null;
    List<ExecutionEntity> processInstanceExecutions = executionEntityManager.findChildExecutionsByProcessInstanceId(execution.getProcessInstanceId());
    for (ExecutionEntity childExecution : processInstanceExecutions) {
      if (childExecution.getCurrentFlowElement() != null && childExecution.getCurrentFlowElement().getId().equals(boundaryEvent.getAttachedToRefId())) {
        subProcessExecution = childExecution;
        break;
      }
    }
    
    if (subProcessExecution == null) {
      throw new ActivitiException("No execution found for sub process of boundary cancel event " + boundaryEvent.getId());
    }
    
    EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
    List<CompensateEventSubscriptionEntity> eventSubscriptions = eventSubscriptionEntityManager.getCompensateEventSubscriptions(subProcessExecution.getParentId());

    if (eventSubscriptions.isEmpty()) {
      leave(execution);
    } else {
      // cancel boundary is always sync
      ScopeUtil.throwCompensationEvent(eventSubscriptions, execution, false);
      executionEntityManager.deleteExecutionAndRelatedData(subProcessExecution);
      if (subProcessExecution.getCurrentFlowElement() instanceof Activity) {
        Activity activity = (Activity) subProcessExecution.getCurrentFlowElement();
        if (activity.getLoopCharacteristics() != null) {
          ExecutionEntity miExecution = subProcessExecution.getParent();
          List<ExecutionEntity> miChildExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(miExecution.getId());
          for (ExecutionEntity miChildExecution : miChildExecutions) {
            if (subProcessExecution.getId().equals(miChildExecution.getId()) == false && activity.getId().equals(miChildExecution.getCurrentActivityId())) {
              executionEntityManager.deleteExecutionAndRelatedData(miChildExecution);
            }
          }
        }
      }
      leave(execution);
    }
  }
}
