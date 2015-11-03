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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for implementing BPMN 2.0 activities, offering convenience
 * methods specific to BPMN 2.0.
 * 
 * This class can be used by inheritance or aggregation.
 * 
 * @author Joram Barrez
 */
public class BpmnActivityBehavior implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  private static Logger log = LoggerFactory.getLogger(BpmnActivityBehavior.class);

  /**
   * Performs the default outgoing BPMN 2.0 behavior, which is having parallel
   * paths of executions for the outgoing sequence flow.
   * 
   * More precisely: every sequence flow that has a condition which evaluates to
   * true (or which doesn't have a condition), is selected for continuation of
   * the process instance. If multiple sequencer flow are selected, multiple,
   * parallel paths of executions are created.
   */
  public void performDefaultOutgoingBehavior(ActivityExecution activityExecution) {
    ActivityImpl activity = (ActivityImpl) activityExecution.getActivity();
    if (!(activity.getActivityBehavior() instanceof IntermediateCatchEventActivityBehavior)) {
      dispatchJobCanceledEvents(activityExecution);
    }
    performOutgoingBehavior(activityExecution, true, false, null);
  }

  /**
   * Performs the default outgoing BPMN 2.0 behavior (@see
   * {@link #performDefaultOutgoingBehavior(ActivityExecution)}), but without
   * checking the conditions on the outgoing sequence flow.
   * 
   * This means that every outgoing sequence flow is selected for continuing the
   * process instance, regardless of having a condition or not. In case of
   * multiple outgoing sequence flow, multiple parallel paths of executions will
   * be created.
   */
  public void performIgnoreConditionsOutgoingBehavior(ActivityExecution activityExecution) {
    performOutgoingBehavior(activityExecution, false, false, null);
  }

  /**
   * dispatch job canceled event for job associated with given execution entity
   * @param activityExecution
   */
  protected void dispatchJobCanceledEvents(ActivityExecution activityExecution) {
    if (activityExecution instanceof ExecutionEntity) {
      List<JobEntity> jobs = ((ExecutionEntity) activityExecution).getJobs();
      for (JobEntity job: jobs) {
        if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
          Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
            ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, job));
        }
      }
    }
  }

  /**
   * Actual implementation of leaving an activity.
   * 
   * @param execution
   *          The current execution context
   * @param checkConditions
   *          Whether or not to check conditions before determining whether or
   *          not to take a transition.
   * @param throwExceptionIfExecutionStuck
   *          If true, an {@link ActivitiException} will be thrown in case no
   *          transition could be found to leave the activity.
   */
  protected void performOutgoingBehavior(ActivityExecution execution, 
          boolean checkConditions, boolean throwExceptionIfExecutionStuck, List<ActivityExecution> reusableExecutions) {

    if (log.isDebugEnabled()) {
      log.debug("Leaving activity '{}'", execution.getActivity().getId());
    }

    String defaultSequenceFlow = (String) execution.getActivity().getProperty("default");
    List<PvmTransition> transitionsToTake = new ArrayList<PvmTransition>();

    List<PvmTransition> outgoingTransitions = execution.getActivity().getOutgoingTransitions();
    for (PvmTransition outgoingTransition : outgoingTransitions) {
      Expression skipExpression = outgoingTransition.getSkipExpression();
      
      if (!SkipExpressionUtil.isSkipExpressionEnabled(execution, skipExpression)) {
        if (defaultSequenceFlow == null || !outgoingTransition.getId().equals(defaultSequenceFlow)) {
          Condition condition = (Condition) outgoingTransition.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
          if (condition == null || !checkConditions || condition.evaluate(outgoingTransition.getId(), execution)) {
            transitionsToTake.add(outgoingTransition);
          }
        }
        
      } else if (SkipExpressionUtil.shouldSkipFlowElement(execution, skipExpression)){
        transitionsToTake.add(outgoingTransition);
      }
    }

    if (transitionsToTake.size() == 1) {
      
      execution.take(transitionsToTake.get(0));

    } else if (transitionsToTake.size() >= 1) {

      execution.inactivate();
      if (reusableExecutions == null || reusableExecutions.isEmpty()) {
        execution.takeAll(transitionsToTake, Arrays.asList(execution));
      } else {
        execution.takeAll(transitionsToTake, reusableExecutions);
      }

    } else {

      if (defaultSequenceFlow != null) {
        PvmTransition defaultTransition = execution.getActivity().findOutgoingTransition(defaultSequenceFlow);
        if (defaultTransition != null) {
          execution.take(defaultTransition);
        } else {
          throw new ActivitiException("Default sequence flow '" + defaultSequenceFlow + "' could not be not found");
        }
      } else {
        
        Object isForCompensation = execution.getActivity().getProperty(BpmnParse.PROPERTYNAME_IS_FOR_COMPENSATION);
        if(isForCompensation != null && (Boolean) isForCompensation) {
          if (execution instanceof ExecutionEntity) {
            Context.getCommandContext().getHistoryManager().recordActivityEnd((ExecutionEntity) execution);
          }
          InterpretableExecution parentExecution = (InterpretableExecution) execution.getParent();
          ((InterpretableExecution)execution).remove();
          parentExecution.signal("compensationDone", null);

        } else {
          
          if (log.isDebugEnabled()) {
            log.debug("No outgoing sequence flow found for {}. Ending execution.", execution.getActivity().getId());
          }
          execution.end();
          
          if (throwExceptionIfExecutionStuck) {
            throw new ActivitiException("No outgoing sequence flow of the inclusive gateway '" + execution.getActivity().getId()
                  + "' could be selected for continuing the process");
          }
        }
        
      }
    }
  }

}
