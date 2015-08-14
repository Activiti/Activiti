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

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Transaction;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class SequentialMultiInstanceBehavior extends MultiInstanceActivityBehavior {

  private static final long serialVersionUID = 1L;

  public SequentialMultiInstanceBehavior(Activity activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
    super(activity, innerActivityBehavior);
  }

  /**
   * Handles the sequential case of spawning the instances. Will only create one instance, since at most one instance can be active.
   */
  protected void createInstances(ActivityExecution execution) {
    int nrOfInstances = resolveNrOfInstances(execution);
    if (nrOfInstances < 0) {
      throw new ActivitiIllegalArgumentException("Invalid number of instances: must be a non-negative integer value" + ", but was " + nrOfInstances);
    }

    setLoopVariable(execution, NUMBER_OF_INSTANCES, nrOfInstances);
    setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, 0);
    setLoopVariable(execution, getCollectionElementIndexVariable(), 0);
    setLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES, 1);
    logLoopDetails(execution, "initialized", 0, 0, 1, nrOfInstances);

    if (nrOfInstances > 0) {
      executeOriginalBehavior(execution, 0);
    }
  }

  /**
   * Called when the wrapped {@link ActivityBehavior} calls the {@link AbstractBpmnActivityBehavior#leave(ActivityExecution)} method. Handles the completion of one instance, and executes the logic for
   * the sequential behavior.
   */
  public void leave(ActivityExecution execution) {
    int loopCounter = getLoopVariable(execution, getCollectionElementIndexVariable()) + 1;
    int nrOfInstances = getLoopVariable(execution, NUMBER_OF_INSTANCES);
    int nrOfCompletedInstances = getLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES) + 1;
    int nrOfActiveInstances = getLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES);

    setLoopVariable(execution, getCollectionElementIndexVariable(), loopCounter);
    setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances);
    logLoopDetails(execution, "instance completed", loopCounter, nrOfCompletedInstances, nrOfActiveInstances, nrOfInstances);
    
    callActivityEndListeners(execution);
    
    //executeCompensationBoundaryEvents(execution.getCurrentFlowElement(), execution);

    if (loopCounter >= nrOfInstances || completionConditionSatisfied(execution)) {
      boolean hasCompensation = false;
      Activity activity = (Activity) execution.getCurrentFlowElement(); 
      if (activity instanceof Transaction) {
        hasCompensation = true;
      } else if (activity instanceof SubProcess) {
        SubProcess subProcess = (SubProcess) activity;
        for (FlowElement subElement : subProcess.getFlowElements()) {
          if (subElement instanceof Activity) {
            Activity subActivity = (Activity) subElement;
            if (CollectionUtils.isNotEmpty(subActivity.getBoundaryEvents())) {
              for (BoundaryEvent boundaryEvent : subActivity.getBoundaryEvents()) {
                if (CollectionUtils.isNotEmpty(boundaryEvent.getEventDefinitions()) && 
                    boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
                  
                  hasCompensation = true;
                  break;
                }
              }
            }
          }
        }
      }
      
      if (hasCompensation) {
        ExecutionEntity executionEntity = (ExecutionEntity) execution;
        ScopeUtil.createCopyOfSubProcessExecutionForCompensation(executionEntity, executionEntity.getParent());
      }
      
      removeLocalLoopVariable(execution, getCollectionElementIndexVariable());
      super.leave(execution);
      
    } else {
      try {
        executeOriginalBehavior(execution, loopCounter);
      } catch (BpmnError error) {
        // re-throw business fault so that it can be caught by an Error
        // Intermediate Event or Error Event Sub-Process in the process
        throw error;
      } catch (Exception e) {
        throw new ActivitiException("Could not execute inner activity behavior of multi instance behavior", e);
      }
    }
  }
}
