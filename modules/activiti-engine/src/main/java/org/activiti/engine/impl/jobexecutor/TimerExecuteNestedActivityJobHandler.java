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
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TimerExecuteNestedActivityJobHandler implements JobHandler {
  
  private static Logger log = LoggerFactory.getLogger(TimerExecuteNestedActivityJobHandler.class);
  
  public static final String TYPE = "timer-transition";

  public String getType() {
    return TYPE;
  }
  
  public void execute(JobEntity job, String configuration, ExecutionEntity execution, CommandContext commandContext) {
    ActivityImpl borderEventActivity = execution.getProcessDefinition().findActivity(configuration);

    if (borderEventActivity == null) {
      throw new ActivitiException("Error while firing timer: border event activity " + configuration + " not found");
    }

    try {
      if (commandContext.getEventDispatcher().isEnabled()) {
        commandContext.getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TIMER_FIRED, job));
        dispatchTaskTimeout(job, execution, commandContext);
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

  protected void dispatchTaskTimeout(JobEntity timerEntity, ExecutionEntity execution, CommandContext commandContext) {
    // in the case of timer start event executionID can be null
      ActivityImpl boundaryEventActivity = execution.getProcessDefinition().findActivity(timerEntity.getJobHandlerConfiguration());
      ActivityBehavior boundaryActivityBehavior = boundaryEventActivity.getActivityBehavior();
      if (boundaryActivityBehavior instanceof BoundaryEventActivityBehavior) {
        // when the timer event is boundary timer event
        BoundaryEventActivityBehavior boundaryEventActivityBehavior = (BoundaryEventActivityBehavior) boundaryActivityBehavior;
        if (boundaryEventActivityBehavior.isInterrupting()) {
          // activiti to which timer is bounded
          ActivityImpl activity = execution.getActivity();
          if (activity != null && activity.getActivityBehavior() instanceof UserTaskActivityBehavior) {
            // if activity to which timer is bounded is user task
            List<Task> tasks = getTasks(execution, activity);
            for (Task task : tasks) {
              log.debug("Dispatching new TASK_TIMEOUT event for task with id {}", task.getId());
              commandContext.getEventDispatcher().dispatchEvent(
                ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_TIMEOUT, task));
            }
          }
        }
      }
    }

  private List<Task> getTasks(Execution execution, ActivityImpl activity) {
    TaskQueryImpl taskQuery = new TaskQueryImpl(Context.getCommandContext());
    taskQuery.executionId(execution.getId());
    taskQuery.taskDefinitionKey(activity.getId());
    return Context.getCommandContext().getTaskEntityManager().findTasksByQueryCriteria(taskQuery);
  }


}
