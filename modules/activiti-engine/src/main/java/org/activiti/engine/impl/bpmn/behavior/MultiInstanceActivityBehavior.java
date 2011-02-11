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
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.runtime.ExecutionEntity;


/**
 * Implementation of the multi-instance functionality as described in the BPMN 2.0 spec.
 * 
 * Multi instance functionality is implemented as an {@link ActivityBehavior} that
 * wraps the original {@link ActivityBehavior} of the activity.
 *
 * Only subclasses of {@link AbstractBpmnActivityBehavior} can have multi-instance
 * behavior. As such, special logic is contained in the {@link AbstractBpmnActivityBehavior}
 * to delegate to the {@link MultiInstanceActivityBehavior} if needed.
 * 
 * @author Joram Barrez
 */
public class MultiInstanceActivityBehavior extends FlowNodeActivityBehavior  
  implements CompositeActivityBehavior, SubProcessActivityBehavior {
  
  protected static final Logger LOGGER = Logger.getLogger(MultiInstanceActivityBehavior.class.getName());
  
  // Variable names for outer instance(as described in spec)
  protected final String NUMBER_OF_INSTANCES = "nrOfInstances";
  protected final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
  protected final String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";
  
  // Variable names for inner instances (as described in the spec)
  protected final String LOOP_COUNTER = "loopCounter";
  
  // instance members
  protected AbstractBpmnActivityBehavior originalActivityBehavior;
  protected boolean isSequential;
  protected Expression loopCardinalityExpression;
  protected Expression completionConditionExpression;
  
  /**
   * @param originalActivityBehavior The original {@link ActivityBehavior} of the activity 
   *                         that will be wrapped inside this behavior.
   * @param isSequential Indicates whether the multi instance behavior
   *                     must be sequential or parallel
   */
  public MultiInstanceActivityBehavior(AbstractBpmnActivityBehavior originalActivityBehavior, boolean isSequential) {
    this.originalActivityBehavior = originalActivityBehavior;
    this.originalActivityBehavior.setMultiInstanceActivityBehavior(this);
    this.isSequential = isSequential;
  }
  
  /**
   * Spawns the instances of the activity.
   */
  public void execute(ActivityExecution execution) throws Exception {
    int loopCardinalityValue = resolveLoopCardinality(execution);
    if (loopCardinalityValue <= 0) {
      throw new ActivitiException("Invalid loopCardinality: must be positive integer value" 
              + ", but was " + loopCardinalityValue);
    }
    setLoopVariable(execution, NUMBER_OF_INSTANCES, loopCardinalityValue);
    setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, 0);
    
    if (isSequential || loopCardinalityValue == 1) {
      executeSequentialBehavior(execution, loopCardinalityValue);
    } else {
      executeParallelBehavior(execution, loopCardinalityValue);
    }
  }

  /**
   * Handles the sequential case of spawning the instances.
   * Will only create one instance, since at most one instance can be active.
   */
  protected void executeSequentialBehavior(ActivityExecution execution, int loopCardinalityValue) throws Exception {
    setLoopVariable(execution, LOOP_COUNTER, 0);
    setLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES, 1);
    logLoopDetails(execution, "initialized", 0, 0, 1, loopCardinalityValue);
    originalActivityBehavior.execute(execution);
  }
  
  /**
   * Handles the parallel case of spawning the instances.
   * Will create child executions accordingly for every instance needed.
   */
  protected void executeParallelBehavior(ActivityExecution execution, int loopCardinalityValue) throws Exception {
    setLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES, loopCardinalityValue);
    List<ActivityExecution> concurrentExecutions = new ArrayList<ActivityExecution>();
    for (int loopCounter=0; loopCounter<loopCardinalityValue; loopCounter++) {
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
      logLoopDetails(concurrentExecution, "initialized", loopCounter, 0, loopCardinalityValue, loopCardinalityValue);
    }
    
    // Before the activities are executed, all executions MUST be created up front
    // Do not try to merge this loop with the previous one, as it will lead to bugs,
    // due to possible child execution pruning.
    for (int loopCounter=0; loopCounter<loopCardinalityValue; loopCounter++) {
      ActivityExecution concurrentExecution = concurrentExecutions.get(loopCounter);
      if (concurrentExecution.isActive() && concurrentExecution.getParent().isActive()) { 
        // executions can be inactive, if instances are all automatics (no-waitstate)
        // and completionCondition has been met in the meantime
        setLoopVariable(concurrentExecution, LOOP_COUNTER, loopCounter);
        originalActivityBehavior.execute(concurrentExecution);
      }
    }
  }
  
  /**
   * Intercepts signals, and delegates it to the wrapped {@link ActivityBehavior}.
   */
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    originalActivityBehavior.signal(execution, signalName, signalData);
  }
  
  /**
   * Called when the wrapped {@link ActivityBehavior} calls the 
   * {@link AbstractBpmnActivityBehavior#leave(ActivityExecution)} method.
   */
  protected void leave(ActivityExecution execution) {
    int loopCounter = getLoopVariable(execution, LOOP_COUNTER);
    int nrOfInstances = getLoopVariable(execution, NUMBER_OF_INSTANCES);
    int nrOfCompletedInstances = getLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES);
    int nrOfActiveInstances = getLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES);
    
    if (isSequential) {
      sequentialLeave(execution, loopCounter, nrOfInstances, nrOfCompletedInstances, nrOfActiveInstances);
    } else {
      parallelLeave(execution, loopCounter, nrOfInstances, nrOfCompletedInstances, nrOfActiveInstances);
    }
  }

  /**
   * Handles the completion of one instance, and executes the logic for the sequential behavior.    
   */
  protected void sequentialLeave(ActivityExecution execution, int loopCounter, int nrOfInstances, 
          int nrOfCompletedInstances, int nrOfActiveInstances) {
    loopCounter++;
    nrOfCompletedInstances++;
    setLoopVariable(execution, LOOP_COUNTER, loopCounter);
    setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances);
    logLoopDetails(execution, "instance completed", loopCounter, nrOfCompletedInstances, nrOfActiveInstances, nrOfInstances);
    if (loopCounter == nrOfInstances || completionConditionSatisfied(execution)) {
      super.leave(execution);
    } else {
      try {
        originalActivityBehavior.execute(execution);
      } catch (Exception e) {
        throw new ActivitiException("Could not execute inner activity behavior of multi instance behavior", e);
      }
    }
  }
  
  protected void parallelLeave(ActivityExecution execution, int loopCounter, int nrOfInstances, 
          int nrOfCompletedInstances, int nrOfActiveInstances) {
    nrOfCompletedInstances++;
    nrOfActiveInstances--;
    
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
  
  // required for supporting embedded subprocesses
  public void lastExecutionEnded(ActivityExecution execution) {
    leave(execution);
  }
  
  // required for supporting external subprocesses
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
  }

  // required for supporting external subprocesses
  public void completed(ActivityExecution execution) throws Exception {
    leave(execution);
  }
  
  // Helpers //////////////////////////////////////////////////////////////////////
  
  protected boolean isExtraScopeNeeded() {
    // special care is needed when the behavior is an embedded subprocess
    // (not very clean, but it works)
    return originalActivityBehavior instanceof org.activiti.engine.impl.bpmn.behavior.SubProcessActivityBehavior;  
  }
  
  protected int resolveLoopCardinality(ActivityExecution execution) {
    // Using Number since expr can evaluate to eg. Long (default for Juel)
    Object value = loopCardinalityExpression.getValue(execution);
    if (value instanceof Number) {
      return ((Number) value).intValue();
    } else if (value instanceof String) {
      return Integer.valueOf((String) value);
    } else {
      throw new ActivitiException("Could not resolve loopCardinality expression '" 
              +loopCardinalityExpression.getExpressionText()+"': not a number nor number String");
    }
  }
  
  protected boolean completionConditionSatisfied(ActivityExecution execution) {
    if (completionConditionExpression != null) {
      Object value = completionConditionExpression.getValue(execution);
      if (! (value instanceof Boolean)) {
        throw new ActivitiException("completionCondition '"
                +completionConditionExpression.getExpressionText()
                +"' does not evaluate to a boolean value");
      }
      Boolean booleanValue = (Boolean) value;
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("Completion condition of multi-instance satisfied: " + booleanValue);
      }
      return booleanValue;
    }
    return false;
  }
  
  protected void setLoopVariable(ActivityExecution execution, String variableName, int value) {
    execution.setVariableLocal(variableName, value);
  }
  
  protected int getLoopVariable(ActivityExecution execution, String variableName) {
    Object value = execution.getVariableLocal(variableName);
    ActivityExecution parent = execution.getParent();
    while (value == null && parent != null) {
      value = parent.getVariableLocal(variableName);
      parent = parent.getParent();
    }
    return (Integer) value;
  }
  
  protected void logLoopDetails(ActivityExecution execution, String custom, int loopCounter, 
          int nrOfCompletedInstances, int nrOfActiveInstances, int nrOfInstances) {
    if (LOGGER.isLoggable(Level.FINE)) {
      StringBuilder strb = new StringBuilder();
      strb.append(isSequential ? "Sequential " : "Parallel ");
      strb.append(" multi-instance '" + execution.getActivity() + "' " + custom + ". ");
      strb.append("Details: loopCounter=" + loopCounter + ", ");
      strb.append("nrOrCompletedInstances=" + nrOfCompletedInstances + ", ");
      strb.append("nrOfActiveInstances=" + nrOfActiveInstances+ ", ");
      strb.append("nrOfInstances=" + nrOfInstances);
      LOGGER.fine(strb.toString());
    }
  }
  
  // Getters and Setters ///////////////////////////////////////////////////////////

  public AbstractBpmnActivityBehavior getActivityBehavior() {
    return originalActivityBehavior;
  }
  public void setActivityBehavior(AbstractBpmnActivityBehavior activityBehavior) {
    this.originalActivityBehavior = activityBehavior;
  }
  public boolean isSequential() {
    return isSequential;
  }
  public void setSequential(boolean isSequential) {
    this.isSequential = isSequential;
  }
  public Expression getLoopCardinalityExpression() {
    return loopCardinalityExpression;
  }
  public void setLoopCardinalityExpression(Expression loopCardinalityExpression) {
    this.loopCardinalityExpression = loopCardinalityExpression;
  }
  public Expression getCompletionConditionExpression() {
    return completionConditionExpression;
  }
  public void setCompletionConditionExpression(Expression completionConditionExpression) {
    this.completionConditionExpression = completionConditionExpression;
  }
  
}
