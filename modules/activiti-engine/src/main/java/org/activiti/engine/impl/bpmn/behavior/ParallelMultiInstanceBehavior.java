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
import java.util.logging.Level;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.runtime.ExecutionEntity;


/**
 * @author Joram Barrez
 */
public class ParallelMultiInstanceBehavior extends MultiInstanceActivityBehavior {
  
  public ParallelMultiInstanceBehavior(AbstractBpmnActivityBehavior originalActivityBehavior) {
    super(originalActivityBehavior);
  }
  
  /**
   * Handles the parallel case of spawning the instances.
   * Will create child executions accordingly for every instance needed.
   */
  public void execute(ActivityExecution execution) throws Exception {
    int nrOfInstances = resolveNrOfInstances(execution);
    if (nrOfInstances <= 0) {
      throw new ActivitiException("Invalid number of instances: must be positive integer value" 
              + ", but was " + nrOfInstances);
    }
    
    setLoopVariable(execution, NUMBER_OF_INSTANCES, nrOfInstances);
    setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, 0);
    setLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES, nrOfInstances);
    
    List<ActivityExecution> concurrentExecutions = new ArrayList<ActivityExecution>();
    for (int loopCounter=0; loopCounter<nrOfInstances; loopCounter++) {
      ActivityExecution concurrentExecution = execution.createExecution();
      concurrentExecution.setActive(true);
      concurrentExecution.setConcurrent(true);
      concurrentExecution.setScope(false);
      
      if (isExtraScopeNeeded()) {
        ActivityExecution extraScopedExecution = concurrentExecution.createExecution();
        extraScopedExecution.setActive(true);
        extraScopedExecution.setConcurrent(false);
        extraScopedExecution.setScope(true);
        concurrentExecution = extraScopedExecution;
      } 
      
      concurrentExecutions.add(concurrentExecution);
      logLoopDetails(concurrentExecution, "initialized", loopCounter, 0, nrOfInstances, nrOfInstances);
    }
    
    // Before the activities are executed, all executions MUST be created up front
    // Do not try to merge this loop with the previous one, as it will lead to bugs,
    // due to possible child execution pruning.
    for (int loopCounter=0; loopCounter<nrOfInstances; loopCounter++) {
      ActivityExecution concurrentExecution = concurrentExecutions.get(loopCounter);
      if (concurrentExecution.isActive() && concurrentExecution.getParent().isActive()) { 
        // executions can be inactive, if instances are all automatics (no-waitstate)
        // and completionCondition has been met in the meantime
        setLoopVariable(concurrentExecution, LOOP_COUNTER, loopCounter);
        executeOriginalBehavior(concurrentExecution, loopCounter);
      }
    }
  }
  
  /**
   * Called when the wrapped {@link ActivityBehavior} calls the 
   * {@link AbstractBpmnActivityBehavior#leave(ActivityExecution)} method.
   * Handles the completion of one of the parallel instances
   */
  public void leave(ActivityExecution execution) {
    int loopCounter = getLoopVariable(execution, LOOP_COUNTER);
    int nrOfInstances = getLoopVariable(execution, NUMBER_OF_INSTANCES);
    int nrOfCompletedInstances = getLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES) + 1;
    int nrOfActiveInstances = getLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES) - 1;
    
    if (isExtraScopeNeeded()) {
      // In case an extra scope was created, it must be destroyed first before going further
      ExecutionEntity temp = (ExecutionEntity) execution;
      execution = execution.getParent();
      temp.remove();
    }
    
    setLoopVariable(execution.getParent(), NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances);
    setLoopVariable(execution.getParent(), NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances);
    logLoopDetails(execution, "instance completed", loopCounter, nrOfCompletedInstances, nrOfActiveInstances, nrOfInstances);
    
    execution.inactivate();
    ((ExecutionEntity) execution.getParent()).forceUpdate();
    
    List<ActivityExecution> joinedExecutions = execution.findInactiveConcurrentExecutions(execution.getActivity());
    if (joinedExecutions.size() == nrOfInstances || completionConditionSatisfied(execution)) {
      
      // Removing all active child executions (ie because completionCondition is true)
      List<ExecutionEntity> executionsToRemove = new ArrayList<ExecutionEntity>();
      for (ActivityExecution childExecution : execution.getParent().getExecutions()) {
        if (childExecution.isActive()) {
          executionsToRemove.add((ExecutionEntity) childExecution);
        }
      }
      for (ExecutionEntity executionToRemove : executionsToRemove) {
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine("Execution " + executionToRemove + " still active, "
                  + "but multi-instance is completed. Removing this execution.");
        }
        executionToRemove.inactivate();
        executionToRemove.deleteCascade("multi-instance completed");
      }
      
      execution.takeAll(execution.getActivity().getOutgoingTransitions(), joinedExecutions);
    } 
  }

}
