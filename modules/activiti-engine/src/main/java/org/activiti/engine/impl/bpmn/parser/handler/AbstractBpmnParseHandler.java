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
package org.activiti.engine.impl.bpmn.parser.handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.parse.BpmnParseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Doccen: do not use, only for internal usage
 * (or we must extract a second subclass).
 * 
 * @author Joram Barrez
 */
public abstract class AbstractBpmnParseHandler<T extends BaseElement> implements BpmnParseHandler {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBpmnParseHandler.class);
  
  public static final String PROPERTYNAME_IS_FOR_COMPENSATION = "isForCompensation";
  
  public static final String PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION = "eventDefinitions";
  
  public static final String PROPERTYNAME_ERROR_EVENT_DEFINITIONS = "errorEventDefinitions";
  
  public static final String PROPERTYNAME_TIMER_DECLARATION = "timerDeclarations";
  
  public Set<Class< ? extends BaseElement>> getHandledTypes() {
    Set<Class< ? extends BaseElement>> types = new HashSet<Class<? extends BaseElement>>();
    types.add(getHandledType());
    return types;
  }
  
  protected Class<? extends BaseElement> getHandledType() {
    // Subclasses should override
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public void parse(BpmnParse bpmnParse, BaseElement element) {
    T baseElement = (T) element;
    executeParse(bpmnParse, baseElement);
  }
  
  protected abstract void executeParse(BpmnParse bpmnParse, T element);
  
  protected ActivityImpl findActivity(BpmnParse bpmnParse, String id) {
    return bpmnParse.getCurrentScope().findActivity(id);
  }
  
  public ActivityImpl createActivityOnCurrentScope(BpmnParse bpmnParse, FlowElement flowElement, String xmlLocalName) {
    return createActivityOnScope(bpmnParse, flowElement, xmlLocalName, bpmnParse.getCurrentScope());
  }
  
  public ActivityImpl createActivityOnScope(BpmnParse bpmnParse, FlowElement flowElement, String xmlLocalName, ScopeImpl scopeElement) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Parsing activity {}", flowElement.getId());
    }
    
    ActivityImpl activity = scopeElement.createActivity(flowElement.getId());
    bpmnParse.setCurrentActivity(activity);

    activity.setProperty("name", flowElement.getName());
    activity.setProperty("documentation", flowElement.getDocumentation());
    if (flowElement instanceof Activity) {
      Activity modelActivity = (Activity) flowElement;
      activity.setProperty("default", modelActivity.getDefaultFlow());
      if(modelActivity.isForCompensation()) {
        activity.setProperty(PROPERTYNAME_IS_FOR_COMPENSATION, true);        
      }
    } else if (flowElement instanceof Gateway) {
      activity.setProperty("default", ((Gateway) flowElement).getDefaultFlow());
    }
    activity.setProperty("type", xmlLocalName);
    
    return activity;
  }
  
  protected void createExecutionListenersOnScope(BpmnParse bpmnParse, List<ActivitiListener> activitiListenerList, ScopeImpl scope) {
    for (ActivitiListener activitiListener : activitiListenerList) {
      scope.addExecutionListener(activitiListener.getEvent(), createExecutionListener(bpmnParse, activitiListener));
    }
  }
  
  protected void createExecutionListenersOnTransition(BpmnParse bpmnParse, List<ActivitiListener> activitiListenerList, TransitionImpl transition) {
    for (ActivitiListener activitiListener : activitiListenerList) {
      transition.addExecutionListener(createExecutionListener(bpmnParse, activitiListener));
    }
  }
  
  protected ExecutionListener createExecutionListener(BpmnParse bpmnParse, ActivitiListener activitiListener) {
    ExecutionListener executionListener = null;
  
    if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(activitiListener.getImplementationType())) {
      executionListener = bpmnParse.getListenerFactory().createClassDelegateExecutionListener(activitiListener);  
    } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
      executionListener = bpmnParse.getListenerFactory().createExpressionExecutionListener(activitiListener);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
      executionListener = bpmnParse.getListenerFactory().createDelegateExpressionExecutionListener(activitiListener);
    }
    return executionListener;
  }
  
  @SuppressWarnings("unchecked")
  protected void addEventSubscriptionDeclaration(BpmnParse bpmnParse, EventSubscriptionDeclaration subscription, EventDefinition parsedEventDefinition, ScopeImpl scope) {
    List<EventSubscriptionDeclaration> eventDefinitions = (List<EventSubscriptionDeclaration>) scope.getProperty(PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION);
    if(eventDefinitions == null) {
      eventDefinitions = new ArrayList<EventSubscriptionDeclaration>();
      scope.setProperty(PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION, eventDefinitions);
    } else {
      // if this is a message event, validate that it is the only one with the provided name for this scope
      if(subscription.getEventType().equals("message")) {
        for (EventSubscriptionDeclaration eventDefinition : eventDefinitions) {
          if(eventDefinition.getEventType().equals("message")
            && eventDefinition.getEventName().equals(subscription.getEventName()) 
            && eventDefinition.isStartEvent() == subscription.isStartEvent()) {
            
            bpmnParse.getBpmnModel().addProblem("Cannot have more than one message event subscription with name '" + subscription.getEventName() +
                "' for scope '"+scope.getId()+"'", parsedEventDefinition);
          }
        }
      }
    }  
    eventDefinitions.add(subscription);
  }
  
  protected String getPrecedingEventBasedGateway(BpmnParse bpmnParse, IntermediateCatchEvent event) {
    String eventBasedGatewayId = null;
    for (SequenceFlow sequenceFlow : event.getIncomingFlows()) {
      FlowElement sourceElement = bpmnParse.getBpmnModel().getFlowElement(sequenceFlow.getSourceRef());
      if (sourceElement instanceof EventGateway) {
        eventBasedGatewayId = sourceElement.getId();
        break;
      }
    }
    return eventBasedGatewayId;
  }
  

}
