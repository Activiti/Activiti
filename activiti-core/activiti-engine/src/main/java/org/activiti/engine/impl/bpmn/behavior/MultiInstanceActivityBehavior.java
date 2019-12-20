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
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
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


 */
public abstract class MultiInstanceActivityBehavior extends FlowNodeActivityBehavior implements SubProcessActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected static final Logger LOGGER = LoggerFactory.getLogger(MultiInstanceActivityBehavior.class);

  // Variable names for outer instance(as described in spec)
  protected static final String NUMBER_OF_INSTANCES = "nrOfInstances";
  protected static final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
  protected static final String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";

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

  private String loopDataOutputRef;
  private String outputDataItem;

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

  public void execute(DelegateExecution execution) {
    if (getLocalLoopVariable(execution, getCollectionElementIndexVariable()) == null) {

      int nrOfInstances = 0;

      try {
        nrOfInstances = createInstances(execution);
      } catch (BpmnError error) {
        ErrorPropagation.propagateError(error, execution);
      }

      if (nrOfInstances == 0) {
        super.leave(execution);
      }

    } else {
      Context.getCommandContext().getHistoryManager().recordActivityStart((ExecutionEntity) execution);

      innerActivityBehavior.execute(execution);
    }
  }

  protected abstract int createInstances(DelegateExecution execution);

  protected void executeCompensationBoundaryEvents(FlowElement flowElement, DelegateExecution execution) {

    //Execute compensation boundary events
    Collection<BoundaryEvent> boundaryEvents = findBoundaryEventsForFlowNode(execution.getProcessDefinitionId(), flowElement);
    if (CollectionUtil.isNotEmpty(boundaryEvents)) {

      // The parent execution becomes a scope, and a child execution is created for each of the boundary events
      for (BoundaryEvent boundaryEvent : boundaryEvents) {

        if (CollectionUtil.isEmpty(boundaryEvent.getEventDefinitions())) {
          continue;
        }

        if (boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
          ExecutionEntity childExecutionEntity = Context.getCommandContext().getExecutionEntityManager()
              .createChildExecution((ExecutionEntity) execution);
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
  public void trigger(DelegateExecution execution, String signalName, Object signalData) {
    innerActivityBehavior.trigger(execution, signalName, signalData);
  }

  // required for supporting embedded subprocesses
  public void lastExecutionEnded(DelegateExecution execution) {
    //ScopeUtil.createEventScopeExecution((ExecutionEntity) execution);
    leave(execution);
  }

  // required for supporting external subprocesses
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
  }

  // required for supporting external subprocesses
  public void completed(DelegateExecution execution) throws Exception {
    leave(execution);
  }

  // Helpers
  // //////////////////////////////////////////////////////////////////////

  @SuppressWarnings("rawtypes")
  protected int resolveNrOfInstances(DelegateExecution execution) {
    if (loopCardinalityExpression != null) {
      return resolveLoopCardinality(execution);

    } else if(usesCollection()) {
      Collection collection = resolveAndValidateCollection(execution);
      return collection.size();

    } else {
      throw new ActivitiIllegalArgumentException("Couldn't resolve collection expression nor variable reference");
    }
  }

  @SuppressWarnings("rawtypes")
  protected void executeOriginalBehavior(DelegateExecution execution, int loopCounter) {
    if (usesCollection() && collectionElementVariable != null) {
      Collection collection = (Collection) resolveCollection(execution);

      Object value = null;
      int index = 0;
      Iterator it = collection.iterator();
      while (index <= loopCounter) {
        value = it.next();
        index++;
      }
      setLoopVariable(execution, collectionElementVariable, value);
    }

    execution.setCurrentFlowElement(activity);
    Context.getAgenda().planContinueMultiInstanceOperation((ExecutionEntity) execution);
  }

  @SuppressWarnings("rawtypes")
  protected Collection resolveAndValidateCollection(DelegateExecution execution) {
    Object obj = resolveCollection(execution);
    if (collectionExpression != null) {
      if (!(obj instanceof Collection)) {
        throw new ActivitiIllegalArgumentException(collectionExpression.getExpressionText() + "' didn't resolve to a Collection");
      }

    } else if (collectionVariable != null) {
      if (obj == null) {
        throw new ActivitiIllegalArgumentException("Variable " + collectionVariable + " is not found");
      }

      if (!(obj instanceof Collection)) {
        throw new ActivitiIllegalArgumentException("Variable " + collectionVariable + "' is not a Collection");
      }

    } else {
      throw new ActivitiIllegalArgumentException("Couldn't resolve collection expression nor variable reference");
    }
    return (Collection) obj;
  }

  protected Object resolveCollection(DelegateExecution execution) {
    Object collection = null;
    if (collectionExpression != null) {
      collection = collectionExpression.getValue(execution);

    } else if (collectionVariable != null) {
      collection = execution.getVariable(collectionVariable);
    }
    return collection;
  }

  protected boolean usesCollection() {
    return collectionExpression != null || collectionVariable != null;
  }

  protected boolean isExtraScopeNeeded(FlowNode flowNode) {
    return flowNode.getSubProcess() != null;
  }

  protected int resolveLoopCardinality(DelegateExecution execution) {
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

  protected boolean completionConditionSatisfied(DelegateExecution execution) {
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

  protected void setLoopVariable(DelegateExecution execution, String variableName, Object value) {
    execution.setVariableLocal(variableName, value);
  }

  protected Integer getLoopVariable(DelegateExecution execution, String variableName) {
    Object value = execution.getVariableLocal(variableName);
    DelegateExecution parent = execution.getParent();
    while (value == null && parent != null) {
      value = parent.getVariableLocal(variableName);
      parent = parent.getParent();
    }
    return (Integer) (value != null ? value : 0);
  }

  protected Integer getLocalLoopVariable(DelegateExecution execution, String variableName) {
    return (Integer) execution.getVariableLocal(variableName);
  }

  protected void removeLocalLoopVariable(DelegateExecution execution, String variableName) {
    execution.removeVariableLocal(variableName);
  }

  /**
   * Since no transitions are followed when leaving the inner activity, it is needed to call the end listeners yourself.
   */
  protected void callActivityEndListeners(DelegateExecution execution) {
    Context.getCommandContext().getProcessEngineConfiguration().getListenerNotificationHelper()
      .executeExecutionListeners(activity, execution, ExecutionListener.EVENTNAME_END);
  }

  protected void logLoopDetails(DelegateExecution execution, String custom, int loopCounter, int nrOfCompletedInstances, int nrOfActiveInstances, int nrOfInstances) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Multi-instance '{}' {}. Details: loopCounter={}, nrOrCompletedInstances={},nrOfActiveInstances={},nrOfInstances={}",
          execution.getCurrentFlowElement() != null ? execution.getCurrentFlowElement().getId() : "", custom, loopCounter,
          nrOfCompletedInstances, nrOfActiveInstances, nrOfInstances);
    }
  }

  protected DelegateExecution getMultiInstanceRootExecution(DelegateExecution executionEntity) {
    DelegateExecution multiInstanceRootExecution = null;
    DelegateExecution currentExecution = executionEntity;
    while (currentExecution != null  && multiInstanceRootExecution == null && currentExecution.getParent() != null) {
      if (currentExecution.isMultiInstanceRoot()) {
        multiInstanceRootExecution = currentExecution;
      } else {
        currentExecution = currentExecution.getParent();
      }
    }
    return multiInstanceRootExecution;
  }

  protected void dispatchActivityCompletedEvent(DelegateExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    Context.getCommandContext().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createActivityEvent(
            ActivitiEventType.ACTIVITY_COMPLETED,
            executionEntity.getActivityId(),
            executionEntity.getName(),
            executionEntity.getId(),
            executionEntity.getProcessInstanceId(),
            executionEntity.getProcessDefinitionId(),
            executionEntity.getCurrentFlowElement()
    ));
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

    public String getLoopDataOutputRef() {
        return loopDataOutputRef;
    }

    public boolean hasLoopDataOutputRef() {
      return loopDataOutputRef != null && !loopDataOutputRef.trim().isEmpty();
    }

    public void setLoopDataOutputRef(String loopDataOutputRef) {
        this.loopDataOutputRef = loopDataOutputRef;
    }

    public String getOutputDataItem() {
        return outputDataItem;
    }

    public void setOutputDataItem(String outputDataItem) {
        this.outputDataItem = outputDataItem;
    }

    protected void updateResultCollection(DelegateExecution childExecution,
        DelegateExecution miRootExecution) {
        if (miRootExecution != null && hasLoopDataOutputRef()) {
            Object loopDataOutputReference = miRootExecution
                .getVariableLocal(getLoopDataOutputRef());
            List<Object> resultCollection;
            if (loopDataOutputReference instanceof List) {
                resultCollection = (List<Object>) loopDataOutputReference;
            } else {
                resultCollection = new ArrayList<>();
            }
            resultCollection.add(childExecution.getVariable(getOutputDataItem()));
            setLoopVariable(miRootExecution, getLoopDataOutputRef(), resultCollection);
        }
    }

    protected void propagateLoopDataOutputRefToProcessInstance(ExecutionEntity miRootExecution) {
        if (hasLoopDataOutputRef()) {
            miRootExecution.getProcessInstance().setVariable(getLoopDataOutputRef(), miRootExecution.getVariable(getLoopDataOutputRef()));
        }
    }
}
