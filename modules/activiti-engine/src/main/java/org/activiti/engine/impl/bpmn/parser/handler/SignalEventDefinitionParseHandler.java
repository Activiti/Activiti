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
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.apache.commons.lang.StringUtils;


/**
 * @author Joram Barrez
 */
public class SignalEventDefinitionParseHandler extends AbstractMultiInstanceEnabledParseHandler<SignalEventDefinition> {
  
  public Class< ? extends BaseElement> getHandledType() {
    return SignalEventDefinition.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, SignalEventDefinition signalDefinition, ScopeImpl scope, ActivityImpl activity, SubProcess subProcess) {
    
    if (bpmnParse.getBpmnModel().containsSignalId(signalDefinition.getSignalRef())) {
      String signalName = bpmnParse.getBpmnModel().getSignal(signalDefinition.getSignalRef()).getName();
      if (StringUtils.isEmpty(signalName)) {
        bpmnParse.getBpmnModel().addProblem("signalName is required for a signal event", signalDefinition);
      }
      signalDefinition.setSignalRef(signalName);
    }
    
    if (bpmnParse.getCurrentFlowElement() instanceof StartEvent) {
    
      EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(signalDefinition.getSignalRef(), "signal");
      eventSubscriptionDeclaration.setActivityId(activity.getId());
      eventSubscriptionDeclaration.setStartEvent(false);
      addEventSubscriptionDeclaration(bpmnParse, eventSubscriptionDeclaration, signalDefinition, scope);
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof IntermediateCatchEvent){
      
      activity.setProperty("type", "intermediateSignalCatch");   
      
      EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(signalDefinition.getSignalRef(), "signal");
      if (getPrecedingEventBasedGateway(bpmnParse, (IntermediateCatchEvent) bpmnParse.getCurrentFlowElement()) != null) {
        eventSubscriptionDeclaration.setActivityId(activity.getId());
        addEventSubscriptionDeclaration(bpmnParse, eventSubscriptionDeclaration, signalDefinition, activity.getParent());      
      } else {
        activity.setScope(true);
        addEventSubscriptionDeclaration(bpmnParse, eventSubscriptionDeclaration, signalDefinition, activity);   
      }
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof ThrowEvent) {
      
      activity.setProperty("type", "intermediateSignalThrow");  
      EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(signalDefinition.getSignalRef(), "signal");
      eventSubscriptionDeclaration.setAsync(signalDefinition.isAsync());
      
      activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createIntermediateThrowSignalEventActivityBehavior((ThrowEvent) bpmnParse.getCurrentFlowElement(), eventSubscriptionDeclaration)); 
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof BoundaryEvent) {
      
      BoundaryEvent boundaryEvent = (BoundaryEvent) bpmnParse.getCurrentFlowElement();
      boolean interrupting = boundaryEvent.isCancelActivity();
      activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createBoundaryEventActivityBehavior(boundaryEvent, interrupting, activity));
      
      activity.setProperty("type", "boundarySignal");
        
      EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(signalDefinition.getSignalRef(), "signal");
      eventSubscriptionDeclaration.setActivityId(activity.getId());
      addEventSubscriptionDeclaration(bpmnParse, eventSubscriptionDeclaration, signalDefinition, activity.getParent());
        
      if (activity.getParent() instanceof ActivityImpl) {     
        ((ActivityImpl) activity.getParent()).setScope(true);
      }
        
    }
    
    
  }

}
