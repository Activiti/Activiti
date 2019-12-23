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
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;

/**

 */
public class BoundaryCancelEventActivityBehavior extends BoundaryEventActivityBehavior {

  private static final long serialVersionUID = 1L;

  @Override
  public void trigger(DelegateExecution execution, String triggerName, Object triggerData) {
    BoundaryEvent boundaryEvent = (BoundaryEvent) execution.getCurrentFlowElement();
    
    CommandContext commandContext = Context.getCommandContext();
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    
    ExecutionEntity subProcessExecution = null;
    // TODO: this can be optimized. A full search in the all executions shouldn't be needed
    List<ExecutionEntity> processInstanceExecutions = executionEntityManager.findChildExecutionsByProcessInstanceId(execution.getProcessInstanceId());
    for (ExecutionEntity childExecution : processInstanceExecutions) {
      if (childExecution.getCurrentFlowElement() != null 
          && childExecution.getCurrentFlowElement().getId().equals(boundaryEvent.getAttachedToRefId())) {
        subProcessExecution = childExecution;
        break;
      }
    }
    
    if (subProcessExecution == null) {
      throw new ActivitiException("No execution found for sub process of boundary cancel event " + boundaryEvent.getId());
    }
    
    EventSubscriptionEntityManager eventSubscriptionEntityManager = commandContext.getEventSubscriptionEntityManager();
    List<CompensateEventSubscriptionEntity> eventSubscriptions = eventSubscriptionEntityManager.findCompensateEventSubscriptionsByExecutionId(subProcessExecution.getParentId());

    if (eventSubscriptions.isEmpty()) {
      leave(execution);
    } else {
      
      String deleteReason = DeleteReason.BOUNDARY_EVENT_INTERRUPTING + "(" + boundaryEvent.getId() + ")";
      
      // cancel boundary is always sync
      ScopeUtil.throwCompensationEvent(eventSubscriptions, execution, false);
      executionEntityManager.deleteExecutionAndRelatedData(subProcessExecution, deleteReason);
      if (subProcessExecution.getCurrentFlowElement() instanceof Activity) {
        Activity activity = (Activity) subProcessExecution.getCurrentFlowElement();
        if (activity.getLoopCharacteristics() != null) {
          ExecutionEntity miExecution = subProcessExecution.getParent();
          List<ExecutionEntity> miChildExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(miExecution.getId());
          for (ExecutionEntity miChildExecution : miChildExecutions) {
            if (subProcessExecution.getId().equals(miChildExecution.getId()) == false && activity.getId().equals(miChildExecution.getCurrentActivityId())) {
              executionEntityManager.deleteExecutionAndRelatedData(miChildExecution, deleteReason);
            }
          }
        }
      }
      leave(execution);
    }
  }
}
