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

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.bpmn.parser.factory.ListenerFactory;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.history.handler.ActivityInstanceStartHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the multi-instance functionality as described in the BPMN 2.0 spec.
 * 
 * Multi instance functionality is implemented as an {@link ActivityBehavior} that wraps the original {@link ActivityBehavior} of the activity.
 * 
 * Only subclasses of {@link AbstractBpmnActivityBehavior} can have multi-instance behavior. As such, special logic is contained in the {@link AbstractBpmnActivityBehavior} to delegate to the
 * {@link MultiInstanceActivityBehavior} if needed.
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public abstract class MultiInstanceActivityBehavior extends FlowNodeActivityBehavior implements SubProcessActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected static final Logger LOGGER = LoggerFactory.getLogger(MultiInstanceActivityBehavior.class);

  // Variable names for outer instance(as described in spec)
  protected final String NUMBER_OF_INSTANCES = "nrOfInstances";
  protected final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
  protected final String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";

  // Instance members
  protected Activity activity;
  protected AbstractBpmnActivityBehavior innerActivityBehavior;
  protected Expression loopCardinalityExpression;
  protected Expression completionConditionExpression;
  protected Expression collectionExpression;
  protected String collectionVariable;
  protected String collectionElementVariable;
  // default variable name for loop counter for inner instances (as described in the spec)
  protected String collectionElementIndexVariable = "loopCounter";

  /**
   * @param innerActivityBehavior
   *          The original {@link ActivityBehavior} of the activity that will be wrapped inside this behavior.
   * @param isSequential
   *          Indicates whether the multi instance behavior must be sequential or parallel
   */
  public MultiInstanceActivityBehavior(Activity activity, AbstractBpmnActivityBehavior innerActivityBehavior) {
    this.activity = activity;
    setInnerActivityBehavior(innerActivityBehavior);
  }

  public void execute(ActivityExecution execution) {
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

  protected abstract void createInstances(ActivityExecution execution);
  
  protected void executeCompensationBoundaryEvents(FlowElement flowElement, ActivityExecution execution) {

    //Execute compensation boundary events
    Collection<BoundaryEvent> boundaryEvents = findBoundaryEventsForFlowNode(execution.getProcessDefinitionId(), flowElement);
    if (CollectionUtils.isNotEmpty(boundaryEvents)) {
      
      // The parent execution becomes a scope, and a child execution is created for each of the boundary events
      for (BoundaryEvent boundaryEvent : boundaryEvents) {

        if (CollectionUtils.isEmpty(boundaryEvent.getEventDefinitions())) {
          continue;
        }

        if (boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
          ExecutionEntity childExecutionEntity = (ExecutionEntity) execution.createExecution();
          childExecutionEntity.setParentId(execution.getId());
          childExecutionEntity.setCurrentFlowElement(boundaryEvent);
          childExecutionEntity.setScope(false);

          ActivityBehavior boundaryEventBehavior = ((ActivityBehavior) boundaryEvent.getBehavior());
          boundaryEventBehavior.execute(childExecutionEntity);
        }
      }
    }
  }
  
  protected Collection<BoundaryEvent> findBoundaryEventsForFlowNode(final String processDefinitionId, final FlowElement flowElement) {
    Process process = getProcessDefinition(processDefinitionId);

    // This could be cached or could be done at parsing time
    List<BoundaryEvent> results = new ArrayList<BoundaryEvent>(1);
    Collection<BoundaryEvent> boundaryEvents = process.findFlowElementsOfType(BoundaryEvent.class, true);
    for (BoundaryEvent boundaryEvent : boundaryEvents) {
      if (boundaryEvent.getAttachedToRefId() != null && boundaryEvent.getAttachedToRefId().equals(flowElement.getId())) {
        results.add(boundaryEvent);
      }
    }
    return results;
  }
  
  protected Process getProcessDefinition(String processDefinitionId) {
    return ProcessDefinitionUtil.getProcess(processDefinitionId);
  }

  // Intercepts signals, and delegates it to the wrapped {@link ActivityBehavior}.
  public void trigger(ActivityExecution execution, String signalName, Object signalData) {
    innerActivityBehavior.trigger(execution, signalName, signalData);
  }

  // required for supporting embedded subprocesses
  public void lastExecutionEnded(ActivityExecution execution) {
    //ScopeUtil.createEventScopeExecution((ExecutionEntity) execution);
    leave(execution);
  }

  // required for supporting external subprocesses
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
  }

  // required for supporting external subprocesses
  public void completed(ActivityExecution execution) throws Exception {
    leave(execution);
  }

  // Helpers
  // //////////////////////////////////////////////////////////////////////

  @SuppressWarnings("rawtypes")
  protected int resolveNrOfInstances(ActivityExecution execution) {
    int nrOfInstances = -1;
    if (loopCardinalityExpression != null) {
      nrOfInstances = resolveLoopCardinality(execution);
    } else if (collectionExpression != null) {
      Object obj = collectionExpression.getValue(execution);
      if (!(obj instanceof Collection)) {
        throw new ActivitiIllegalArgumentException(collectionExpression.getExpressionText() + "' didn't resolve to a Collection");
      }
      nrOfInstances = ((Collection) obj).size();
    } else if (collectionVariable != null) {
      Object obj = execution.getVariable(collectionVariable);
      if (obj == null) {
        throw new ActivitiIllegalArgumentException("Variable " + collectionVariable + " is not found");
      }
      if (!(obj instanceof Collection)) {
        throw new ActivitiIllegalArgumentException("Variable " + collectionVariable + "' is not a Collection");
      }
      nrOfInstances = ((Collection) obj).size();
    } else {
      throw new ActivitiIllegalArgumentException("Couldn't resolve collection expression nor variable reference");
    }
    return nrOfInstances;
  }

  @SuppressWarnings("rawtypes")
  protected void executeOriginalBehavior(ActivityExecution execution, int loopCounter) {
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

    // If loopcounter == 0, then historic activity instance already created,
    // no need to pass through executeActivity again since it will create a new historic activity
    if (loopCounter == 0) {
      callCustomActivityStartListeners(execution);
      innerActivityBehavior.execute(execution);
    } else {
      execution.setCurrentFlowElement(activity);
      Context.getAgenda().planContinueMultiInstanceOperation(execution);
    }
  }

  protected boolean usesCollection() {
    return collectionExpression != null || collectionVariable != null;
  }

  protected boolean isExtraScopeNeeded(FlowNode flowNode) {
    return flowNode.getSubProcess() != null;
  }

  protected int resolveLoopCardinality(ActivityExecution execution) {
    // Using Number since expr can evaluate to eg. Long (which is also the default for Juel)
    Object value = loopCardinalityExpression.getValue(execution);
    if (value instanceof Number) {
      return ((Number) value).intValue();
    } else if (value instanceof String) {
      return Integer.valueOf((String) value);
    } else {
      throw new ActivitiIllegalArgumentException("Could not resolve loopCardinality expression '" + loopCardinalityExpression.getExpressionText() + "': not a number nor number String");
    }
  }

  protected boolean completionConditionSatisfied(ActivityExecution execution) {
    if (completionConditionExpression != null) {
      Object value = completionConditionExpression.getValue(execution);
      if (!(value instanceof Boolean)) {
        throw new ActivitiIllegalArgumentException("completionCondition '" + completionConditionExpression.getExpressionText() + "' does not evaluate to a boolean value");
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
  
  protected void removeLocalLoopVariable(ActivityExecution execution, String variableName) {
    execution.removeVariableLocal(variableName);
  }
  
  /**
   * Since the first loop of the multi instance is not executed as a regular activity,
   * it is needed to call the start listeners yourself.
   */
  protected void callCustomActivityStartListeners(ActivityExecution execution) {
    
    // TODO: needs to be made generic with callActivityEndListeners and calling activiti listeners in general!
    
    List<ActivitiListener> listeners = activity.getExecutionListeners();
    if (CollectionUtils.isNotEmpty(listeners)) {
      
      ListenerFactory listenerFactory = Context.getProcessEngineConfiguration().getListenerFactory();
      
      for (ActivitiListener activitiListener : listeners) {
        if ("start".equalsIgnoreCase(activitiListener.getEvent())) {
          
          // TODO: this needs to be put in a util class or something
          ExecutionListener executionListener = null;

          if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(activitiListener.getImplementationType())) {
            
            // Sad that we have to do this, but it's the only way I could find (which is also safe for backwards compatibility)
            if (!ActivityInstanceStartHandler.class.getName().equals(activitiListener.getImplementation())) {
              executionListener = listenerFactory.createClassDelegateExecutionListener(activitiListener);
            }
            
          } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createExpressionExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createDelegateExpressionExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_INSTANCE.equalsIgnoreCase(activitiListener.getImplementationType())) {
            Object executionListenerInstance = activitiListener.getInstance();
            if (executionListenerInstance instanceof ExecutionListener) {
              executionListener = (ExecutionListener) executionListenerInstance;
            } else {
              LOGGER.warn("Execution listener instance " + executionListenerInstance + " is not of type " + ExecutionListener.class);
            }
          }

          if (executionListener != null) {
            ((ExecutionEntity) execution).setEventName(ExecutionListener.EVENTNAME_START);
            executionListener.notify(execution);
            
            // TODO: is this still needed? Is this property still needed?
            ((ExecutionEntity) execution).setEventName(null);
          }
          
        }
      }
    }
    
  }

  /**
   * Since no transitions are followed when leaving the inner activity, it is needed to call the end listeners yourself.
   */
  protected void callActivityEndListeners(ActivityExecution execution) {
    List<ActivitiListener> listeners = activity.getExecutionListeners();
    if (CollectionUtils.isNotEmpty(listeners)) {
      
      ListenerFactory listenerFactory = Context.getProcessEngineConfiguration().getListenerFactory();
      
      for (ActivitiListener activitiListener : listeners) {
        if ("end".equalsIgnoreCase(activitiListener.getEvent())) {
          
          // TODO: this needs to be put in a util class or something
          ExecutionListener executionListener = null;

          if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createClassDelegateExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createExpressionExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createDelegateExpressionExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_INSTANCE.equalsIgnoreCase(activitiListener.getImplementationType())) {
            Object executionListenerInstance = activitiListener.getInstance();
            if (executionListenerInstance instanceof ExecutionListener) {
              executionListener = (ExecutionListener) executionListenerInstance;
            } else {
              LOGGER.warn("Execution listener instance " + executionListenerInstance + " is not of type " + ExecutionListener.class);
            }
          }

          if (executionListener != null) {
            ((ExecutionEntity) execution).setEventName(ExecutionListener.EVENTNAME_END);
            executionListener.notify(execution);
            
            // TODO: is this still needed? Is this property still needed?
            ((ExecutionEntity) execution).setEventName(null);
          }
          
        }
      }
    }
    
  }

  protected void logLoopDetails(ActivityExecution execution, String custom, int loopCounter, int nrOfCompletedInstances, int nrOfActiveInstances, int nrOfInstances) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Multi-instance '{}' {}. Details: loopCounter={}, nrOrCompletedInstances={},nrOfActiveInstances={},nrOfInstances={}", execution.getCurrentFlowElement(), custom, loopCounter,
          nrOfCompletedInstances, nrOfActiveInstances, nrOfInstances);
    }
  }

  // Getters and Setters
  // ///////////////////////////////////////////////////////////

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
}
