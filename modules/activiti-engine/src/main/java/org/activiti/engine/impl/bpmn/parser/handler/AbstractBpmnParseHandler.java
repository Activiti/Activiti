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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataSpec;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.data.Data;
import org.activiti.engine.impl.bpmn.data.DataRef;
import org.activiti.engine.impl.bpmn.data.IOSpecification;
import org.activiti.engine.impl.bpmn.data.ItemDefinition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.parse.BpmnParseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Joram Barrez
 */
public abstract class AbstractBpmnParseHandler<T extends BaseElement> implements BpmnParseHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractBpmnParseHandler.class);
  
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
  
  protected abstract Class<? extends BaseElement> getHandledType();
  
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
            
            logger.warn("Cannot have more than one message event subscription with name '" + subscription.getEventName() +
                "' for scope '"+scope.getId()+"'");
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
  
  protected IOSpecification createIOSpecification(BpmnParse bpmnParse, org.activiti.bpmn.model.IOSpecification specificationModel) {
    IOSpecification ioSpecification = new IOSpecification();

    for (DataSpec dataInputElement : specificationModel.getDataInputs()) {
      ItemDefinition itemDefinition = bpmnParse.getItemDefinitions().get(dataInputElement.getItemSubjectRef());
      Data dataInput = new Data(bpmnParse.getTargetNamespace() + ":" + dataInputElement.getId(), dataInputElement.getId(), itemDefinition);
      ioSpecification.addInput(dataInput);
    }

    for (DataSpec dataOutputElement : specificationModel.getDataOutputs()) {
      ItemDefinition itemDefinition = bpmnParse.getItemDefinitions().get(dataOutputElement.getItemSubjectRef());
      Data dataOutput = new Data(bpmnParse.getTargetNamespace() + ":" + dataOutputElement.getId(), dataOutputElement.getId(), itemDefinition);
      ioSpecification.addOutput(dataOutput);
    }

    for (String dataInputRef : specificationModel.getDataInputRefs()) {
      DataRef dataRef = new DataRef(dataInputRef);
      ioSpecification.addInputRef(dataRef);
    }

    for (String dataOutputRef : specificationModel.getDataOutputRefs()) {
      DataRef dataRef = new DataRef(dataOutputRef);
      ioSpecification.addOutputRef(dataRef);
    }

    return ioSpecification;
  }
  
  protected void processArtifacts(BpmnParse bpmnParse, Collection<Artifact> artifacts, ScopeImpl scope) {
    // associations  
    for (Artifact artifact : artifacts) {
      if (artifact instanceof Association) {
        createAssociation(bpmnParse, (Association) artifact, scope);
      }
    }
  }
  
  protected void createAssociation(BpmnParse bpmnParse, Association association, ScopeImpl parentScope) {
    BpmnModel bpmnModel = bpmnParse.getBpmnModel();
    if (bpmnModel.getArtifact(association.getSourceRef()) != null ||
        bpmnModel.getArtifact(association.getTargetRef()) != null) {
      
      // connected to a text annotation so skipping it
      return;
    }
    
    ActivityImpl sourceActivity = parentScope.findActivity(association.getSourceRef());
    ActivityImpl targetActivity = parentScope.findActivity(association.getTargetRef());
    
    // an association may reference elements that are not parsed as activities (like for instance 
    // text annotations so do not throw an exception if sourceActivity or targetActivity are null)
    // However, we make sure they reference 'something':
    if (sourceActivity == null) {
      //bpmnModel.addProblem("Invalid reference sourceRef '" + association.getSourceRef() + "' of association element ", association.getId());
    } else if (targetActivity == null) {
      //bpmnModel.addProblem("Invalid reference targetRef '" + association.getTargetRef() + "' of association element ", association.getId());
    } else {      
      if (sourceActivity.getProperty("type").equals("compensationBoundaryCatch")) {
        Object isForCompensation = targetActivity.getProperty(PROPERTYNAME_IS_FOR_COMPENSATION);          
        if (isForCompensation == null || !(Boolean) isForCompensation) {
          logger.warn("compensation boundary catch must be connected to element with isForCompensation=true");
        } else {            
          ActivityImpl compensatedActivity = sourceActivity.getParentActivity();
          compensatedActivity.setProperty(BpmnParse.PROPERTYNAME_COMPENSATION_HANDLER_ID, targetActivity.getId());            
        }
      }
    }
  }
  
  protected Map<String, Object> processDataObjects(BpmnParse bpmnParse, Collection<ValuedDataObject> dataObjects, ScopeImpl scope) {
    Map<String, Object> variablesMap = new HashMap<String, Object>();
    // convert data objects to process variables  
    if (dataObjects != null) {
      for (ValuedDataObject dataObject : dataObjects) {
        variablesMap.put(dataObject.getName(), dataObject.getValue());
      }
    }
    return variablesMap;
  }
}
