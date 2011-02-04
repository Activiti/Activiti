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

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.runtime.ExecutionEntity;


/**
 * @author Joram Barrez
 */
public class MultiInstanceActivityBehavior extends FlowNodeActivityBehavior { 
  
  // Variables for outer instances (as described in spec)
  protected final String NUMBER_OF_INSTANCES = "numberOfInstances";
  protected final String NUMBER_OF_ACTIVE_INSTANCES = "numberOfActiveInstances";
  protected final String NUMBER_OF_COMPLETED_INSTANCES = "numberOfCompletedInstances";
  
  // Variables for inner instances (as described in the spec)
  protected final String LOOP_COUNTER = "loopCounter";
  
  protected AbstractBpmnActivityBehavior activityBehavior;
  protected boolean isSequential;
  protected Expression loopCardinalityExpression;
  
  protected ActivityImpl innerActivity;
  
  public MultiInstanceActivityBehavior(AbstractBpmnActivityBehavior activityBehavior, boolean isSequential) {
    this.activityBehavior = activityBehavior;
    this.activityBehavior.setMultiInstanceActivityBehavior(this);
    this.isSequential = isSequential;
  }
  
  public MultiInstanceActivityBehavior(ActivityImpl nestedActivity) {
    this.innerActivity = nestedActivity;
    ((AbstractBpmnActivityBehavior) this.innerActivity.getActivityBehavior()).setMultiInstanceActivityBehavior(this);
  }

//  public void execute(ActivityExecution execution) throws Exception {
//    int loopCardinalityValue = ((Number) loopCardinalityExpression.getValue(execution)).intValue(); // Long is default for JUEL, see spec Section 1.19
//    if (loopCardinalityValue <= 0) {
//      throw new ActivitiException("Invalid loopCardinality: must be positive integer value" 
//              + ", but was " + loopCardinalityValue);
//    }
//    setLoopVariable(execution, NUMBER_OF_INSTANCES, loopCardinalityValue);
//    setLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES, 1);
//    setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, 0);
//    
//    if (isSequential || loopCardinalityValue == 1) {
//      setLoopVariable(execution, LOOP_COUNTER, 0);
//      activityBehavior.execute(execution);
//    } else {
//      for (int loopCounter=0; loopCounter<loopCardinalityValue; loopCounter++) {
//        ActivityExecution concurrentExecution = execution.createExecution();
//        concurrentExecution.setActive(true);
//        concurrentExecution.setConcurrent(true);
//        concurrentExecution.setScope(false);
//        
//        setLoopVariable(concurrentExecution, LOOP_COUNTER, loopCounter);
//        activityBehavior.execute(concurrentExecution);
//      }
//    }
//  } 
//  
//  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
//    activityBehavior.signal(execution, signalName, signalData);
//  }
//  
//  protected void leave(ActivityExecution execution) {
//    int loopCounter = getLoopVariable(execution, LOOP_COUNTER);
//
//    if (isSequential) {
//      
//      int numberOfInstances = getLoopVariable(execution, NUMBER_OF_INSTANCES);
//      int numberOfCompletedInstances = getLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES);
//      
//      if (loopCounter == numberOfInstances-1) {
//        super.leave(execution);
//      } else {
//        setLoopVariable(execution, LOOP_COUNTER, loopCounter+1);
//        setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, numberOfCompletedInstances+1);
//        try {
//          activityBehavior.execute(execution);
//        } catch (Exception e) {
//          throw new ActivitiException("Could not execute inner activity behavior of multi instance behavior", e);
//        }
//      }
//      
//    } else {
//      
//      int numberOfInstances = getLoopVariable(execution.getParent(), NUMBER_OF_INSTANCES);
//      int numberOfCompletedInstances = getLoopVariable(execution.getParent(), NUMBER_OF_COMPLETED_INSTANCES);
//      
//      execution.inactivate();
//      ((ExecutionEntity) execution.getParent()).forceUpdate();
//      
//      List<ActivityExecution> joinedExecutions = execution.findInactiveConcurrentExecutions(execution.getActivity());
//      if (joinedExecutions.size() == numberOfInstances) {
//        execution.takeAll(execution.getActivity().getOutgoingTransitions(), joinedExecutions);
//      } else {
//        setLoopVariable(execution.getParent(), NUMBER_OF_COMPLETED_INSTANCES, numberOfCompletedInstances++);
//      }
//      
//    }
//  }
  
  public void execute(ActivityExecution execution) throws Exception {
    int loopCardinalityValue = ((Number) loopCardinalityExpression.getValue(execution)).intValue(); // Long is default for JUEL, see spec Section 1.19
    if (loopCardinalityValue <= 0) {
      throw new ActivitiException("Invalid loopCardinality: must be positive integer value" 
              + ", but was " + loopCardinalityValue);
    }
    setOuterActivityVariable(execution, NUMBER_OF_INSTANCES, loopCardinalityValue);
    setOuterActivityVariable(execution, NUMBER_OF_ACTIVE_INSTANCES, 1);
    setOuterActivityVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, 0);
    
    if (isSequential || loopCardinalityValue == 1) {
      setInnerActivityVariable(execution, LOOP_COUNTER, 0);
      execution.executeActivity(innerActivity);
    } else {
      for (int loopCounter=0; loopCounter<loopCardinalityValue; loopCounter++) {
        ActivityExecution concurrentExecution = execution.createExecution();
        concurrentExecution.setActive(true);
        concurrentExecution.setConcurrent(true);
        concurrentExecution.setScope(false);
        
        setInnerActivityVariable(concurrentExecution, LOOP_COUNTER, loopCounter);
        concurrentExecution.executeActivity(innerActivity);
      }
    }
  } 
  
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    ((AbstractBpmnActivityBehavior)innerActivity.getActivityBehavior()).signal(execution, signalName, signalData);
  }
  
  protected void leave(ActivityExecution execution) {
    int loopCounter = getInnerActivityVariable(execution, LOOP_COUNTER);

    if (isSequential) {
      
      int numberOfInstances = getOuterActivityVariable(execution, NUMBER_OF_INSTANCES);
      int numberOfCompletedInstances = getOuterActivityVariable(execution, NUMBER_OF_COMPLETED_INSTANCES);
      
      if (loopCounter == numberOfInstances-1) {
        ((ExecutionEntity) execution).setActivity(innerActivity.getParentActivity());
        super.leave(execution);
      } else {
        setInnerActivityVariable(execution, LOOP_COUNTER, loopCounter+1);
        setOuterActivityVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, numberOfCompletedInstances+1);
        try {
          execution.executeActivity(innerActivity);
        } catch (Exception e) {
          throw new ActivitiException("Could not execute inner activity behavior of multi instance behavior", e);
        }
      }
      
    } else {
      
      int numberOfInstances = getOuterActivityVariable(execution.getParent(), NUMBER_OF_INSTANCES);
      int numberOfCompletedInstances = getOuterActivityVariable(execution.getParent(), NUMBER_OF_COMPLETED_INSTANCES);
      
      execution.inactivate();
      ((ExecutionEntity) execution.getParent()).forceUpdate();
      
      List<ActivityExecution> joinedExecutions = execution.findInactiveConcurrentExecutions(execution.getActivity());
      if (joinedExecutions.size() == numberOfInstances) {
        ((ExecutionEntity) execution).setActivity(innerActivity.getParentActivity());
        execution.takeAll(innerActivity.getParentActivity().getOutgoingTransitions(), joinedExecutions);
      } else {
        setOuterActivityVariable(execution.getParent(), NUMBER_OF_COMPLETED_INSTANCES, numberOfCompletedInstances++);
      }
      
    }
  }
  
  protected void setOuterActivityVariable(ActivityExecution execution, String variableName, int value) {
    execution.setVariableLocal(variableName, value);
  }
  
  protected int getOuterActivityVariable(ActivityExecution execution, String variableName) {
    return (Integer) execution.getVariableLocal(variableName);
  }
  
  protected void setInnerActivityVariable(ActivityExecution execution, String variableName, int value) {
    execution.setVariableLocal(variableName, value);
  }
  
  protected int getInnerActivityVariable(ActivityExecution execution, String variableName) {
    return (Integer) execution.getVariableLocal(variableName);
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
  
}
