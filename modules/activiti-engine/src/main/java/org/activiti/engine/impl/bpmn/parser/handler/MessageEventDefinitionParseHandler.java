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
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;


/**
 * @author Joram Barrez
 */
public class MessageEventDefinitionParseHandler extends AbstractBpmnParseHandler<MessageEventDefinition> {
  
  public Class< ? extends BaseElement> getHandledType() {
    return MessageEventDefinition.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, MessageEventDefinition messageDefinition) {
    
    BpmnModel bpmnModel = bpmnParse.getBpmnModel();
    String messageRef = messageDefinition.getMessageRef();
    if (bpmnModel.containsMessageId(messageRef)) {
      Message message = bpmnModel.getMessage(messageRef);
      messageDefinition.setMessageRef(message.getName());
      messageDefinition.setExtensionElements(message.getExtensionElements());
    }
    
    EventSubscriptionDeclaration eventSubscription = new EventSubscriptionDeclaration(messageDefinition.getMessageRef(), "message");

    ScopeImpl scope = bpmnParse.getCurrentScope();
    ActivityImpl activity = bpmnParse.getCurrentActivity();
    if (bpmnParse.getCurrentFlowElement() instanceof StartEvent && bpmnParse.getCurrentSubProcess() != null) {
      
      // the scope of the event subscription is the parent of the event
      // subprocess (subscription must be created when parent is initialized)
      ScopeImpl catchingScope = ((ActivityImpl) scope).getParent();
      
      EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(messageDefinition.getMessageRef(), "message");
      eventSubscriptionDeclaration.setActivityId(activity.getId());
      eventSubscriptionDeclaration.setStartEvent(false);
      addEventSubscriptionDeclaration(bpmnParse, eventSubscriptionDeclaration, messageDefinition, catchingScope);
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof StartEvent) {
      
      activity.setProperty("type", "messageStartEvent");
      eventSubscription.setStartEvent(true);
      eventSubscription.setActivityId(activity.getId());
      addEventSubscriptionDeclaration(bpmnParse, eventSubscription, messageDefinition, bpmnParse.getCurrentProcessDefinition());
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof IntermediateCatchEvent) {
      
      activity.setProperty("type", "intermediateMessageCatch");   
      
      if(getPrecedingEventBasedGateway(bpmnParse, (IntermediateCatchEvent) bpmnParse.getCurrentFlowElement()) != null) {
        eventSubscription.setActivityId(activity.getId());
        addEventSubscriptionDeclaration(bpmnParse, eventSubscription, messageDefinition, activity.getParent());      
      } else {
        activity.setScope(true);
        addEventSubscriptionDeclaration(bpmnParse, eventSubscription, messageDefinition, activity);   
      }
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof BoundaryEvent) {
      
      BoundaryEvent boundaryEvent = (BoundaryEvent) bpmnParse.getCurrentFlowElement();
      boolean interrupting = boundaryEvent.isCancelActivity();
      activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createBoundaryEventActivityBehavior(boundaryEvent, interrupting, activity));
      
      activity.setProperty("type", "boundaryMessage");
      
      EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(messageDefinition.getMessageRef(), "message");
      eventSubscriptionDeclaration.setActivityId(activity.getId());
      addEventSubscriptionDeclaration(bpmnParse, eventSubscriptionDeclaration, messageDefinition, activity.getParent());
      
      if (activity.getParent() instanceof ActivityImpl) {     
        ((ActivityImpl) activity.getParent()).setScope(true);
      }
    }
    
    
    else {
      // What to do here?
    }
    
  }

}
