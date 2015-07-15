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
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Joram Barrez
 */
public class IntermediateThrowEventParseHandler extends AbstractActivityBpmnParseHandler<ThrowEvent> {
	
	private static final Logger logger = LoggerFactory.getLogger(IntermediateThrowEventParseHandler.class);
  
  public Class< ? extends BaseElement> getHandledType() {
    return ThrowEvent.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, ThrowEvent intermediateEvent) {

    ActivityImpl nestedActivityImpl = createActivityOnCurrentScope(bpmnParse, intermediateEvent, BpmnXMLConstants.ELEMENT_EVENT_THROW);
    
    EventDefinition eventDefinition = null;
    if (!intermediateEvent.getEventDefinitions().isEmpty()) {
      eventDefinition = intermediateEvent.getEventDefinitions().get(0);
    }
    
    nestedActivityImpl.setAsync(intermediateEvent.isAsynchronous());
    nestedActivityImpl.setExclusive(!intermediateEvent.isNotExclusive());
    
    if (eventDefinition instanceof SignalEventDefinition) {
      bpmnParse.getBpmnParserHandlers().parseElement(bpmnParse, eventDefinition);
    } else if (eventDefinition instanceof org.activiti.bpmn.model.CompensateEventDefinition) {
      bpmnParse.getBpmnParserHandlers().parseElement(bpmnParse, eventDefinition);
    } else if (eventDefinition == null) {
      nestedActivityImpl.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createIntermediateThrowNoneEventActivityBehavior(intermediateEvent)); 
    } else { 
      logger.warn("Unsupported intermediate throw event type for throw event " + intermediateEvent.getId());
    }
  }
  
  //
  // Seems not to be used anymore?
  //
//  protected CompensateEventDefinition createCompensateEventDefinition(BpmnParse bpmnParse, org.activiti.bpmn.model.CompensateEventDefinition eventDefinition, ScopeImpl scopeElement) {
//    if(StringUtils.isNotEmpty(eventDefinition.getActivityRef())) {
//      if(scopeElement.findActivity(eventDefinition.getActivityRef()) == null) {
//        bpmnParse.getBpmnModel().addProblem("Invalid attribute value for 'activityRef': no activity with id '" + eventDefinition.getActivityRef() +
//            "' in current scope " + scopeElement.getId(), eventDefinition);
//      }
//    }
//    
//    CompensateEventDefinition compensateEventDefinition =  new CompensateEventDefinition();
//    compensateEventDefinition.setActivityRef(eventDefinition.getActivityRef());
//    compensateEventDefinition.setWaitForCompletion(eventDefinition.isWaitForCompletion());
//    
//    return compensateEventDefinition;
//  }

}
