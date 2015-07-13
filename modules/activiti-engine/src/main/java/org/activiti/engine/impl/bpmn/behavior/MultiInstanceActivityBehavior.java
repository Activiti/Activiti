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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ExecutionListenerInvocation;
import org.activiti.engine.impl.history.handler.ActivityInstanceStartHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
 * @author Falko Menge
 */
public abstract class MultiInstanceActivityBehavior extends FlowNodeActivityBehavior  
  implements CompositeActivityBehavior, SubProcessActivityBehavior {
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(MultiInstanceActivityBehavior.class);
  
  // Variable names for outer instance(as described in spec)
  protected final String NUMBER_OF_INSTANCES = "nrOfInstances";
  protected final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
  protected final String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";
  
  // Instance members
  protected ActivityImpl activity;
  protected AbstractBpmnActivityBehavior innerActivityBehavior;
  protected Expression loopCardinalityExpression;
  protected Expression completionConditionExpression;
  protected Expression collectionExpression;
  protected String collectionVariable;
  protected String collectionElementVariable;
  // default variable name for loop counter for inner instances (as described in the spec)
  protected String collectionElementIndexVariable="loopCounter";

  /**
   * @param innerActivityBehavior The original {@link ActivityBehavior} of the activity 
   *                         that will be wrapped inside this behavior.
   * @param isSequential Indicates whether the multi instance behavior
   *                     must be sequential or parallel
   */
  public MultiInstanceActivityBehavior(ActivityImpl activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
    this.activity = activity;
    setInnerActivityBehavior(innerActivityBehavior);
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    if (getLocalLoopVariable(execution, getCollectionElementIndexVariable()) == null) {
      try {
        createInstances(execution);
      } catch (BpmnError error) {
        ErrorPropagation.propagateError(error, execution);
      }

      if (resolveNrOfInstances(execution) == 0) {
        leave(execution);
      }
    } else {
        innerActivityBehavior.execute(execution);
    }
  }
  
  protected abstract void createInstances(ActivityExecution execution) throws Exception;
  
  // Intercepts signals, and delegates it to the wrapped {@link ActivityBehavior}.
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    innerActivityBehavior.signal(execution, signalName, signalData);
  }
  
  // required for supporting embedded subprocesses
  public void lastExecutionEnded(ActivityExecution execution) {
    ScopeUtil.createEventScopeExecution((ExecutionEntity) execution);
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
  
  @SuppressWarnings("rawtypes")
  protected int resolveNrOfInstances(ActivityExecution execution) {
    int nrOfInstances = -1;
    if (loopCardinalityExpression != null) {
      nrOfInstances = resolveLoopCardinality(execution);
    } else if (collectionExpression != null) {
      Object obj = collectionExpression.getValue(execution);
      if (!(obj instanceof Collection)) {
        throw new ActivitiIllegalArgumentException(collectionExpression.getExpressionText()+"' didn't resolve to a Collection");
      }
      nrOfInstances = ((Collection) obj).size();
    } else if (collectionVariable != null) {
      Object obj = execution.getVariable(collectionVariable);
      if (obj == null) {
        throw new ActivitiIllegalArgumentException("Variable " + collectionVariable + " is not found");
      }
      if (!(obj instanceof Collection)) {
        throw new ActivitiIllegalArgumentException("Variable " + collectionVariable+"' is not a Collection");
      }
      nrOfInstances = ((Collection) obj).size();
    } else {
      throw new ActivitiIllegalArgumentException("Couldn't resolve collection expression nor variable reference");
    }
    return nrOfInstances;
  }
  
  @SuppressWarnings("rawtypes")
  protected void executeOriginalBehavior(ActivityExecution execution, int loopCounter) throws Exception {
    if (usesCollection() && collectionElementVariable != null) {
      Collection collection = null;
      if (collectionExpression != null) {
        collection = (Collection) collectionExpression.getValue(execution);
      } else if (collectionVariable != null) {
        collection = (Collection) execution.getVariable(collectionVariable);
      }
       
      Object value = null;
      int index = 0;
      Iterator it = collection.iterator();
      while (index <= loopCounter) {
        value = it.next();
        index++;
      }
      setLoopVariable(execution, collectionElementVariable, value);
    }

    // If loopcounter == 1, then historic activity instance already created, no need to
    // pass through executeActivity again since it will create a new historic activity
    if (loopCounter == 0) {
    	callCustomActivityStartListeners(execution);
      innerActivityBehavior.execute(execution);
    } else {
      execution.executeActivity(activity);
    }
  }
  
  protected boolean usesCollection() {
    return collectionExpression != null 
              || collectionVariable != null;
  }
  
  protected boolean isExtraScopeNeeded() {
    // special care is needed when the behavior is an embedded subprocess (not very clean, but it works)
    return innerActivityBehavior instanceof org.activiti.engine.impl.bpmn.behavior.SubProcessActivityBehavior;  
  }
  
  protected int resolveLoopCardinality(ActivityExecution execution) {
    // Using Number since expr can evaluate to eg. Long (which is also the default for Juel)
    Object value = loopCardinalityExpression.getValue(execution);
    if (value instanceof Number) {
      return ((Number) value).intValue();
    } else if (value instanceof String) {
      return Integer.valueOf((String) value);
    } else {
      throw new ActivitiIllegalArgumentException("Could not resolve loopCardinality expression '" 
              +loopCardinalityExpression.getExpressionText()+"': not a number nor number String");
    }
  }
  
  protected boolean completionConditionSatisfied(ActivityExecution execution) {
    if (completionConditionExpression != null) {
      Object value = completionConditionExpression.getValue(execution);
      if (! (value instanceof Boolean)) {
        throw new ActivitiIllegalArgumentException("completionCondition '"
                + completionConditionExpression.getExpressionText()
                + "' does not evaluate to a boolean value");
      }
      Boolean booleanValue = (Boolean) value;
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Completion condition of multi-instance satisfied: {}", booleanValue);
      }
      return booleanValue;
    }
    return false;
  }
  
  protected void setLoopVariable(ActivityExecution execution, String variableName, Object value) {
    execution.setVariableLocal(variableName, value);
  }
  
  protected Integer getLoopVariable(ActivityExecution execution, String variableName) {
    Object value = execution.getVariableLocal(variableName);
    ActivityExecution parent = execution.getParent();
    while (value == null && parent != null) {
      value = parent.getVariableLocal(variableName);
      parent = parent.getParent();
    }
    return (Integer) (value != null ? value : 0);
  }

  protected Integer getLocalLoopVariable(ActivityExecution execution, String variableName) {
    return (Integer) execution.getVariableLocal(variableName);
  }
  
  /**
   * Since the first loop of the multi instance is not executed as a regular activity,
   * it is needed to call the start listeners yourself.
   */
  protected void callCustomActivityStartListeners(ActivityExecution execution) {
    List<ExecutionListener> listeners = activity.getExecutionListeners(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START);
    
    List<ExecutionListener> filteredExecutionListeners = new ArrayList<ExecutionListener>(listeners.size());
    if (listeners != null) {
	    // Sad that we have to do this, but it's the only way I could find (which is also safe for backwards compatibility)
	    
	    for (ExecutionListener executionListener : listeners) {
	    	if (!(executionListener instanceof ActivityInstanceStartHandler)) {
	    		filteredExecutionListeners.add(executionListener);
	    	}
	    }
	    
	    CallActivityListenersOperation atomicOperation = new CallActivityListenersOperation(filteredExecutionListeners);
	    Context.getCommandContext().performOperation(atomicOperation, (InterpretableExecution)execution);
    }
    
  }
  
  /**
   * Since no transitions are followed when leaving the inner activity,
   * it is needed to call the end listeners yourself.
   */
  protected void callActivityEndListeners(ActivityExecution execution) {
    List<ExecutionListener> listeners = activity.getExecutionListeners(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END);
    CallActivityListenersOperation atomicOperation = new CallActivityListenersOperation(listeners);
    Context.getCommandContext().performOperation(atomicOperation, (InterpretableExecution)execution);
  }
  
  protected void logLoopDetails(ActivityExecution execution, String custom, int loopCounter, 
          int nrOfCompletedInstances, int nrOfActiveInstances, int nrOfInstances) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Multi-instance '{}' {}. Details: loopCounter={}, nrOrCompletedInstances={},nrOfActiveInstances={},nrOfInstances={}",
              execution.getActivity(), custom, loopCounter, nrOfCompletedInstances, nrOfActiveInstances, nrOfInstances);
    }
  }

  
  // Getters and Setters ///////////////////////////////////////////////////////////
  
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
  public Expression getCollectionExpression() {
    return collectionExpression;
  }
  public void setCollectionExpression(Expression collectionExpression) {
    this.collectionExpression = collectionExpression;
  }
  public String getCollectionVariable() {
    return collectionVariable;
  }
  public void setCollectionVariable(String collectionVariable) {
    this.collectionVariable = collectionVariable;
  }
  public String getCollectionElementVariable() {
    return collectionElementVariable;
  }
  public void setCollectionElementVariable(String collectionElementVariable) {
    this.collectionElementVariable = collectionElementVariable;
  }
  public String getCollectionElementIndexVariable() {
    return collectionElementIndexVariable;
  }
  public void setCollectionElementIndexVariable(String collectionElementIndexVariable) {
    this.collectionElementIndexVariable = collectionElementIndexVariable;
  }
  public void setInnerActivityBehavior(AbstractBpmnActivityBehavior innerActivityBehavior) {
    this.innerActivityBehavior = innerActivityBehavior;
    this.innerActivityBehavior.setMultiInstanceActivityBehavior(this);
  }
  public AbstractBpmnActivityBehavior getInnerActivityBehavior() {
	  return innerActivityBehavior;
  }
  
  /**
   * ACT-1339. Calling ActivityEndListeners within an {@link AtomicOperation} 
   * so that an executionContext is present.
   * 
   * @author Aris Tzoumas
   * @author Joram Barrez
   *
   */
  private static final class CallActivityListenersOperation implements AtomicOperation {

	private List<ExecutionListener> listeners;
	
	private CallActivityListenersOperation(List<ExecutionListener> listeners) {
		this.listeners = listeners;
	}
	
	@Override
	public void execute(InterpretableExecution execution) {
		for (ExecutionListener executionListener : listeners) {
      try {
        Context.getProcessEngineConfiguration()
          .getDelegateInterceptor()
          .handleInvocation(new ExecutionListenerInvocation(executionListener, execution));
      } catch (Exception e) {
        throw new ActivitiException("Couldn't execute listener", e);
      }
    }
	}

	@Override
	public boolean isAsync(InterpretableExecution execution) {
		return false;
	}
	  
  }
}
