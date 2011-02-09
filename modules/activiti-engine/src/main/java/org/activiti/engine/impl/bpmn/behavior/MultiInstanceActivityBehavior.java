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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.runtime.ExecutionEntity;


/**
 * @author Joram Barrez
 */
public class MultiInstanceActivityBehavior extends FlowNodeActivityBehavior  
  implements CompositeActivityBehavior, SubProcessActivityBehavior {
  
  // Variable names for outer instance(as described in spec)
  protected final String NUMBER_OF_INSTANCES = "nrOfInstances";
  protected final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
  protected final String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";
  
  // Variable names for inner instances (as described in the spec)
  protected final String LOOP_COUNTER = "loopCounter";
  
  // members
  protected AbstractBpmnActivityBehavior activityBehavior;
  protected boolean isSequential;
  protected Expression loopCardinalityExpression;
  protected Expression completionConditionExpression;
  
  public MultiInstanceActivityBehavior(AbstractBpmnActivityBehavior activityBehavior, boolean isSequential) {
    this.activityBehavior = activityBehavior;
    this.activityBehavior.setMultiInstanceActivityBehavior(this);
    this.isSequential = isSequential;
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    int loopCardinalityValue = resolveLoopCardinality(execution);
    if (loopCardinalityValue <= 0) {
      throw new ActivitiException("Invalid loopCardinality: must be positive integer value" 
              + ", but was " + loopCardinalityValue);
    }
    setLoopVariable(execution, NUMBER_OF_INSTANCES, loopCardinalityValue);
    setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, 0);
    
    if (isSequential || loopCardinalityValue == 1) {
      setLoopVariable(execution, LOOP_COUNTER, 0);
      setLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES, 1);
      activityBehavior.execute(execution);
    } else {
      List<ActivityExecution> concurrentExecutions = new ArrayList<ActivityExecution>();
      for (int loopCounter=0; loopCounter<loopCardinalityValue; loopCounter++) {
        ActivityExecution concurrentExecution = execution.createExecution();
        concurrentExecution.setActive(true);
        concurrentExecution.setConcurrent(true);
        concurrentExecution.setScope(false);
        concurrentExecutions.add(concurrentExecution);
      }
      
      setLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES, loopCardinalityValue);
      
      // Before the activities are executed, all executions MUST be created up front
      // Do not try to merge this loop with the previous one, as it will lead to bugs,
      // due to possible child execution pruning.
      for (int loopCounter=0; loopCounter<loopCardinalityValue; loopCounter++) {
        ActivityExecution concurrentExecution = concurrentExecutions.get(loopCounter);
        setLoopVariable(concurrentExecution, LOOP_COUNTER, loopCounter);
        activityBehavior.execute(concurrentExecution);
      }
    }
  } 
  
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    activityBehavior.signal(execution, signalName, signalData);
  }
  
  protected void leave(ActivityExecution execution) {
    int loopCounter = getLoopVariable(execution, LOOP_COUNTER);
    int nrOfInstances = getLoopVariable(execution, NUMBER_OF_INSTANCES);
    int nrOfCompletedInstances = getLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES);
    int nrOfActiveInstances = getLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES);
    
    if (isSequential) {
      setLoopVariable(execution, LOOP_COUNTER, loopCounter+1);
      setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances +1);
      
      if (loopCounter == nrOfInstances-1 || completionConditionSatisfied(execution)) {
        super.leave(execution);
      } else {
        try {
          activityBehavior.execute(execution);
        } catch (Exception e) {
          throw new ActivitiException("Could not execute inner activity behavior of multi instance behavior", e);
        }
      }
      
    } else {
      
      setLoopVariable(execution.getParent(), NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances+1);
      setLoopVariable(execution.getParent(), NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances-1);
      
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
          executionToRemove.remove();
        }
        
        execution.takeAll(execution.getActivity().getOutgoingTransitions(), joinedExecutions);
      } else {
      }
      
    }
  }
  
  // required for supporting embedded subprocesses
  public void lastExecutionEnded(ActivityExecution execution) {
    // In case of a sequential multi-instance, we get a 'lastExecutionEnded'
    // for every activity instance that is completed, one at a time.
    // This means we must delegate to the normal leave logic of a multi instance
    //
    // However, in the parallel case, we get a 'lastExecutionEnded'
    // when ALL parallel activity instances are completed, in which case
    // we know we can leave the multi instance activity in the regular BPMN 2.0 way
    if (isSequential) {
      leave(execution);
    } else {
      super.leave(execution);
    }
  }
  
  // required for supporting external subprocesses
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
  }

  // required for supporting external subprocesses
  public void completed(ActivityExecution execution) throws Exception {
    leave(execution);
  }
  
  // Helpers //////////////////////////////////////////////////////////////////////
  
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
      return (Boolean) value;
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
  
  // Getters and Setters ///////////////////////////////////////////////////////////

  public AbstractBpmnActivityBehavior getActivityBehavior() {
    return activityBehavior;
  }
  public void setActivityBehavior(AbstractBpmnActivityBehavior activityBehavior) {
    this.activityBehavior = activityBehavior;
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
