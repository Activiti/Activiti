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

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;


/**
 * @author Joram Barrez
 */
public class IntermediateCatchEventParseHandler extends AbstractFlowNodeBpmnParseHandler<IntermediateCatchEvent> {
  
  public Class< ? extends BaseElement> getHandledType() {
    return IntermediateCatchEvent.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, IntermediateCatchEvent event) {
    
    BpmnModel bpmnModel = bpmnParse.getBpmnModel();
    ActivityImpl nestedActivity = null;
    EventDefinition eventDefinition = null;
    if (event.getEventDefinitions().size() > 0) {
      eventDefinition = event.getEventDefinitions().get(0);
    }
   
    if (eventDefinition == null) {
      
      bpmnModel.addProblem("No event definition for intermediate catch event " + event.getId(), event);
      nestedActivity = createActivityOnCurrentScope(bpmnParse, event, BpmnXMLConstants.ELEMENT_EVENT_CATCH);
      
    } else {
      
      ScopeImpl scope = bpmnParse.getCurrentScope();
      String eventBasedGatewayId = getPrecedingEventBasedGateway(bpmnParse, event);
      if (eventBasedGatewayId  != null) {
        ActivityImpl gatewayActivity = scope.findActivity(eventBasedGatewayId);
        nestedActivity = createActivityOnScope(bpmnParse, event, BpmnXMLConstants.ELEMENT_EVENT_CATCH, gatewayActivity);
      } else {
        nestedActivity = createActivityOnScope(bpmnParse, event, BpmnXMLConstants.ELEMENT_EVENT_CATCH, scope);
      }
      
      // Catch event behavior is the same for all types
      nestedActivity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createIntermediateCatchEventActivityBehavior(event));
      
      if (eventDefinition instanceof TimerEventDefinition
              || eventDefinition instanceof SignalEventDefinition
              || eventDefinition instanceof MessageEventDefinition) {
        
        bpmnParse.getBpmnParserHandlers().parseElement(bpmnParse, eventDefinition);
        
      } else {
        bpmnModel.addProblem("Unsupported intermediate catch event type.", event);
      }
    }
  }
  
}
