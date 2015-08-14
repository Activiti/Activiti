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

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

/**
 * @author Tijs Rademakers
 */
public class IntermediateThrowCompensationEventActivityBehavior extends FlowNodeActivityBehavior {

  private static final long serialVersionUID = 1L;
  
  protected final CompensateEventDefinition compensateEventDefinition;

  public IntermediateThrowCompensationEventActivityBehavior(CompensateEventDefinition compensateEventDefinition) {
    this.compensateEventDefinition = compensateEventDefinition;
  }

  @Override
  public void execute(ActivityExecution execution) {
    ThrowEvent throwEvent = (ThrowEvent) execution.getCurrentFlowElement();
    final String activityRef = compensateEventDefinition.getActivityRef();
    EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
    ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
    
    List<CompensateEventSubscriptionEntity> eventSubscriptions = new ArrayList<CompensateEventSubscriptionEntity>();
    ExecutionEntity subProcessExecution = null;
    
    List<String> rootCompensationActivities = new ArrayList<String>();
    if (throwEvent.getSubProcess() == null) {
      Process process = ProcessDefinitionUtil.getProcess(execution.getProcessDefinitionId());
      for (FlowElement flowElement : process.getFlowElements()) {
        if (flowElement instanceof Activity) {
          Activity activity = (Activity) flowElement;
          if (activity.isForCompensation()) {
            rootCompensationActivities.add(activity.getId());
          }
        }
      }
    }
      
    List<ExecutionEntity> processInstanceExecutions = executionEntityManager.findChildExecutionsByProcessInstanceId(execution.getProcessInstanceId());
    for (ExecutionEntity childExecution : processInstanceExecutions) {
      if (childExecution.getCurrentFlowElement() != null && childExecution.getCurrentFlowElement().getId().equals(activityRef)) {
        subProcessExecution = childExecution;
        break;
      }
    }
    
    if (rootCompensationActivities.isEmpty() && subProcessExecution == null) {
      throw new ActivitiException("No compensation activities found intermediate throw event");
    }
    
    for (String compensationActivity : rootCompensationActivities) {
      eventSubscriptions.addAll(eventSubscriptionEntityManager.getCompensateEventSubscriptionsForProcessInstanceId(execution.getProcessInstanceId(), compensationActivity));
    }
    
    if (subProcessExecution != null) {
      eventSubscriptions.addAll(eventSubscriptionEntityManager.getCompensateEventSubscriptions(subProcessExecution.getParentId()));
    }
    
    if (eventSubscriptions.isEmpty()) {
      leave(execution);
    } else {
      // TODO: implement async (waitForCompletion=false in bpmn)
      ScopeUtil.throwCompensationEvent(eventSubscriptions, execution, false);
      if (subProcessExecution != null) {
        executionEntityManager.deleteExecutionAndRelatedData(subProcessExecution);
      }
      leave(execution);
    }
  }
}
