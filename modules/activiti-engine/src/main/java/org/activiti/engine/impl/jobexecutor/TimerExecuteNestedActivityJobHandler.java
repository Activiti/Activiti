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
package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.util.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TimerExecuteNestedActivityJobHandler extends TimerEventHandler implements JobHandler {
  
  private static Logger log = LoggerFactory.getLogger(TimerExecuteNestedActivityJobHandler.class);
  
  public static final String TYPE = "timer-transition";
  public static final String PROPERTYNAME_TIMER_ACTIVITY_ID = "activityId";
  public static final String PROPERTYNAME_END_DATE_EXPRESSION = "timerEndDate";

  public String getType() {
    return TYPE;
  }
  
  public void execute(JobEntity job, String configuration, ExecutionEntity execution, CommandContext commandContext) {

    String nestedActivityId = TimerEventHandler.getActivityIdFromConfiguration(configuration);

    ActivityImpl borderEventActivity = execution.getProcessDefinition().findActivity(nestedActivityId);

    if (borderEventActivity == null) {
      throw new ActivitiException("Error while firing timer: border event activity " + nestedActivityId + " not found");
    }

    try {
      if (commandContext.getEventDispatcher().isEnabled()) {
        commandContext.getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TIMER_FIRED, job));
        dispatchActivityTimeoutIfNeeded(job, execution, commandContext);
      }

      borderEventActivity
        .getActivityBehavior()
        .execute(execution);
    } catch (RuntimeException e) {
      log.error("exception during timer execution", e);
      throw e;
      
    } catch (Exception e) {
      log.error("exception during timer execution", e);
      throw new ActivitiException("exception during timer execution: "+e.getMessage(), e);
    }
  }

  protected void dispatchActivityTimeoutIfNeeded(JobEntity timerEntity, ExecutionEntity execution, CommandContext commandContext) {

    String nestedActivityId = TimerEventHandler.getActivityIdFromConfiguration(timerEntity.getJobHandlerConfiguration());

    ActivityImpl boundaryEventActivity = execution.getProcessDefinition().findActivity(nestedActivityId);
    ActivityBehavior boundaryActivityBehavior = boundaryEventActivity.getActivityBehavior();
    if (boundaryActivityBehavior instanceof BoundaryEventActivityBehavior) {
      BoundaryEventActivityBehavior boundaryEventActivityBehavior = (BoundaryEventActivityBehavior) boundaryActivityBehavior;
      if (boundaryEventActivityBehavior.isInterrupting()) {
        dispatchExecutionTimeOut(timerEntity, execution, commandContext);
      }
    }
  }

  protected void dispatchExecutionTimeOut(JobEntity timerEntity, ExecutionEntity execution, CommandContext commandContext) {
    // subprocesses
    for (ExecutionEntity subExecution : execution.getExecutions()) {
      dispatchExecutionTimeOut(timerEntity, subExecution, commandContext);
    }

    // call activities
    ExecutionEntity subProcessInstance = commandContext.getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(execution.getId());
    if (subProcessInstance != null) {
      dispatchExecutionTimeOut(timerEntity, subProcessInstance, commandContext);
    }

    // activity with timer boundary event
    ActivityImpl activity = execution.getActivity();
    if (activity != null && activity.getActivityBehavior() != null) {
      dispatchActivityTimeOut(timerEntity, activity, execution, commandContext);
    }
  }

  protected void dispatchActivityTimeOut(JobEntity timerEntity, ActivityImpl activity, ExecutionEntity execution, CommandContext commandContext) {
    commandContext.getEventDispatcher().dispatchEvent(
      ActivitiEventBuilder.createActivityCancelledEvent(activity.getId(),
        (String) activity.getProperties().get("name"),
        execution.getId(),
        execution.getProcessInstanceId(), execution.getProcessDefinitionId(),
        (String) activity.getProperties().get("type"),
        activity.getActivityBehavior().getClass().getCanonicalName(),
        timerEntity
        )
    );
  }

}
