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

import org.activiti.bpmn.model.EndEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;

/**
 * @author Martin Grofcik
 * @author Tijs Rademakers
 */
public class TerminateEndEventActivityBehavior extends FlowNodeActivityBehavior {
  
  private static final long serialVersionUID = 1L;
  
  protected final EndEvent endEvent;
  
  public TerminateEndEventActivityBehavior(EndEvent endEvent) {
    this.endEvent = endEvent.clone();
  }

  public void execute(ActivityExecution execution) throws Exception {
    ActivityImpl terminateEndEventActivity = (ActivityImpl) execution.getActivity();
    ActivityExecution scopeExecution = ScopeUtil.findScopeExecution(execution);

    boolean loop = true;
    // get top superexecution to terminate
    while (scopeExecution.getSuperExecutionId() != null && loop) {
      ActivityExecution superExecution = (ActivityExecution) Context.getProcessEngineConfiguration().getRuntimeService().createExecutionQuery().executionId(scopeExecution.getSuperExecutionId()).singleResult();
      if (superExecution != null) {
        // superExecution can be null in the case when no wait state was reached between super start event and TerminateEndEvent
        while (superExecution.getParent() != null) {
          superExecution = superExecution.getParent();
        }
        scopeExecution = superExecution;
      } else {
        loop = false;
      }
    }
    
    terminateExecution(execution, terminateEndEventActivity, scopeExecution);
  }

  protected void terminateExecution(ActivityExecution execution, ActivityImpl terminateEndEventActivity, ActivityExecution scopeExecution) {
    // send cancelled event
    sendCancelledEvent( execution, terminateEndEventActivity, scopeExecution);

    // destroy the scope
    scopeExecution.destroyScope("terminate end event fired");

    // set the scope execution to the terminate end event and make it end here.
    // (the history should reflect that the execution ended here and we want an 'end time' for the
    // historic activity instance.)
    ((InterpretableExecution)scopeExecution).setActivity(terminateEndEventActivity);
    // end the scope execution
    scopeExecution.end();
  }

  protected void sendCancelledEvent(ActivityExecution execution, ActivityImpl terminateEndEventActivity, ActivityExecution scopeExecution) {
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
              ActivitiEventBuilder.createCancelledEvent(execution.getId(), execution.getProcessInstanceId(),
                      execution.getProcessDefinitionId(), terminateEndEventActivity));
    }
    dispatchExecutionCancelled(scopeExecution, terminateEndEventActivity);
  }

  private void dispatchExecutionCancelled(ActivityExecution execution, ActivityImpl causeActivity) {
    // subprocesses
    for (ActivityExecution subExecution : execution.getExecutions()) {
      dispatchExecutionCancelled(subExecution, causeActivity);
    }

    // call activities
    ExecutionEntity subProcessInstance = Context.getCommandContext().getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(execution.getId());
    if (subProcessInstance != null) {
      dispatchExecutionCancelled(subProcessInstance, causeActivity);
    }

    // activity with message/signal boundary events
    ActivityImpl activity = (ActivityImpl) execution.getActivity();
    if (activity != null && activity.getActivityBehavior() != null && activity != causeActivity) {
      dispatchActivityCancelled(execution, activity, causeActivity);
    }
  }

  protected void dispatchActivityCancelled(ActivityExecution execution, ActivityImpl activity, ActivityImpl causeActivity) {
    Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
            ActivitiEventBuilder.createActivityCancelledEvent(activity.getId(),
                    (String) activity.getProperties().get("name"),
                    execution.getId(),
                    execution.getProcessInstanceId(), execution.getProcessDefinitionId(),
                    (String) activity.getProperties().get("type"),
                    activity.getActivityBehavior().getClass().getCanonicalName(),
                    causeActivity)
    );
  }
  
  public EndEvent getEndEvent() {
    return this.endEvent;
  }

}