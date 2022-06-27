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


package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;

public class SequentialMultiInstanceBehavior extends MultiInstanceActivityBehavior {

  private static final long serialVersionUID = 1L;

  public SequentialMultiInstanceBehavior(Activity activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
    super(activity, innerActivityBehavior);
  }

  /**
   * Handles the sequential case of spawning the instances. Will only create one instance, since at most one instance can be active.
   */
  protected int createInstances(DelegateExecution multiInstanceExecution) {

    int nrOfInstances = resolveNrOfInstances(multiInstanceExecution);
    if (nrOfInstances == 0) {
      return nrOfInstances;
    } else if (nrOfInstances < 0) {
      throw new ActivitiIllegalArgumentException("Invalid number of instances: must be a non-negative integer value" + ", but was " + nrOfInstances);
    }

    // Create child execution that will execute the inner behavior
    ExecutionEntity childExecution = Context.getCommandContext().getExecutionEntityManager()
        .createChildExecution((ExecutionEntity) multiInstanceExecution);
    childExecution.setCurrentFlowElement(multiInstanceExecution.getCurrentFlowElement());
    multiInstanceExecution.setMultiInstanceRoot(true);
    multiInstanceExecution.setActive(false);

    // Set Multi-instance variables
    setLoopVariable(multiInstanceExecution, NUMBER_OF_INSTANCES, nrOfInstances);
    setLoopVariable(multiInstanceExecution, NUMBER_OF_COMPLETED_INSTANCES, 0);
    setLoopVariable(multiInstanceExecution, NUMBER_OF_ACTIVE_INSTANCES, 1);
    setLoopVariable(childExecution, getCollectionElementIndexVariable(), 0);
    logLoopDetails(multiInstanceExecution, "initialized", 0, 0, 1, nrOfInstances);

    executeOriginalBehavior(childExecution, 0);
    return nrOfInstances;
  }

  /**
   * Called when the wrapped {@link ActivityBehavior} calls the {@link AbstractBpmnActivityBehavior#leave(DelegateExecution)} method. Handles the completion of one instance, and executes the logic for
   * the sequential behavior.
   */
  public void leave(DelegateExecution childExecution) {
    DelegateExecution multiInstanceRootExecution = getMultiInstanceRootExecution(childExecution);
    int nrOfInstances = getLoopVariable(multiInstanceRootExecution, NUMBER_OF_INSTANCES);
    int loopCounter = getLoopVariable(childExecution, getCollectionElementIndexVariable()) + 1;
    int nrOfCompletedInstances = getLoopVariable(multiInstanceRootExecution, NUMBER_OF_COMPLETED_INSTANCES) + 1;
    int nrOfActiveInstances = getLoopVariable(multiInstanceRootExecution, NUMBER_OF_ACTIVE_INSTANCES);

    setLoopVariable(multiInstanceRootExecution, NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances);
    setLoopVariable(childExecution, getCollectionElementIndexVariable(), loopCounter);
    logLoopDetails(childExecution, "instance completed", loopCounter, nrOfCompletedInstances, nrOfActiveInstances, nrOfInstances);

    updateResultCollection(childExecution, multiInstanceRootExecution);

    Context.getCommandContext().getHistoryManager().recordActivityEnd((ExecutionEntity) childExecution, null);
    callActivityEndListeners(childExecution);

    if (loopCounter >= nrOfInstances || completionConditionSatisfied(multiInstanceRootExecution)) {
      propagateLoopDataOutputRefToProcessInstance((ExecutionEntity) multiInstanceRootExecution);
      removeLocalLoopVariable(childExecution, getCollectionElementIndexVariable());
      multiInstanceRootExecution.setMultiInstanceRoot(false);
      multiInstanceRootExecution.setScope(false);
      multiInstanceRootExecution.setCurrentFlowElement(childExecution.getCurrentFlowElement());
      Context.getCommandContext().getExecutionEntityManager().deleteChildExecutions((ExecutionEntity) multiInstanceRootExecution, "MI_END");
      dispatchActivityCompletedEvent(childExecution);
      super.leave(multiInstanceRootExecution);

    } else {
      try {

        if (childExecution.getCurrentFlowElement() instanceof SubProcess) {
          ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
          ExecutionEntity executionToContinue = executionEntityManager.createChildExecution((ExecutionEntity) multiInstanceRootExecution);
          executionToContinue.setCurrentFlowElement(childExecution.getCurrentFlowElement());
          executionToContinue.setScope(true);
          setLoopVariable(executionToContinue, getCollectionElementIndexVariable(), loopCounter);
          executeOriginalBehavior(executionToContinue, loopCounter);
        } else {
          executeOriginalBehavior(childExecution, loopCounter);
        }
        dispatchActivityCompletedEvent(childExecution);
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
