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

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

/**
 * @author Joram Barrez
 */
public class SignalEventDefinitionParseHandler extends AbstractBpmnParseHandler<SignalEventDefinition> {
  
  public Class< ? extends BaseElement> getHandledType() {
    return SignalEventDefinition.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, SignalEventDefinition signalDefinition) {
    
    Signal signal = null;
    if (bpmnParse.getBpmnModel().containsSignalId(signalDefinition.getSignalRef())) {
      signal = bpmnParse.getBpmnModel().getSignal(signalDefinition.getSignalRef());
      String signalName = signal.getName();
      signalDefinition.setSignalRef(signalName);
    }
    
    if (signal == null) {
      return;
    }
    
    ActivityImpl activity = bpmnParse.getCurrentActivity();
    if (bpmnParse.getCurrentFlowElement() instanceof StartEvent) {
      
      activity.setProperty("type", "signalStartEvent");
    
      EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(signalDefinition.getSignalRef(), "signal");
      eventSubscriptionDeclaration.setActivityId(activity.getId());
      eventSubscriptionDeclaration.setStartEvent(true);
      addEventSubscriptionDeclaration(bpmnParse, eventSubscriptionDeclaration, signalDefinition, bpmnParse.getCurrentScope());
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof IntermediateCatchEvent){
      
      activity.setProperty("type", "intermediateSignalCatch");   
      
      EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(signalDefinition.getSignalRef(), "signal");
      
      if (signal.getScope() != null) {
        eventSubscriptionDeclaration.setConfiguration(signal.getScope());
      }
      
      if (getPrecedingEventBasedGateway(bpmnParse, (IntermediateCatchEvent) bpmnParse.getCurrentFlowElement()) != null) {
        eventSubscriptionDeclaration.setActivityId(activity.getId());
        addEventSubscriptionDeclaration(bpmnParse, eventSubscriptionDeclaration, signalDefinition, activity.getParent());      
      } else {
        activity.setScope(true);
        addEventSubscriptionDeclaration(bpmnParse, eventSubscriptionDeclaration, signalDefinition, activity);   
      }
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof ThrowEvent) {
      
      ThrowEvent throwEvent = (ThrowEvent) bpmnParse.getCurrentFlowElement();
      
      activity.setProperty("type", "intermediateSignalThrow");  
      EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(signalDefinition.getSignalRef(), "signal");
      eventSubscriptionDeclaration.setAsync(signalDefinition.isAsync());
      
      activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createIntermediateThrowSignalEventActivityBehavior(throwEvent, signal, eventSubscriptionDeclaration)); 
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof BoundaryEvent) {
      
      BoundaryEvent boundaryEvent = (BoundaryEvent) bpmnParse.getCurrentFlowElement();
      boolean interrupting = boundaryEvent.isCancelActivity();
      activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createBoundaryEventActivityBehavior(boundaryEvent, interrupting, activity));
      
      activity.setProperty("type", "boundarySignal");
        
      EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(signalDefinition.getSignalRef(), "signal");
      eventSubscriptionDeclaration.setActivityId(activity.getId());
      
      if (signal.getScope() != null) {
        eventSubscriptionDeclaration.setConfiguration(signal.getScope());
      }
      
      addEventSubscriptionDeclaration(bpmnParse, eventSubscriptionDeclaration, signalDefinition, activity.getParent());
        
      if (activity.getParent() instanceof ActivityImpl) {     
        ((ActivityImpl) activity.getParent()).setScope(true);
      }
        
    }
    
    
  }

}
