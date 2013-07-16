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
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.CompensateEventDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Joram Barrez
 */
public class IntermediateThrowEventParseHandler extends AbstractActivityBpmnParseHandler<ThrowEvent> {
  
  public Class< ? extends BaseElement> getHandledType() {
    return ThrowEvent.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, ThrowEvent intermediateEvent) {

    BpmnModel bpmnModel = bpmnParse.getBpmnModel();

    ActivityImpl nestedActivityImpl = createActivityOnCurrentScope(bpmnParse, intermediateEvent, BpmnXMLConstants.ELEMENT_EVENT_THROW);
    
    EventDefinition eventDefinition = null;
    if (intermediateEvent.getEventDefinitions().size() > 0) {
      eventDefinition = intermediateEvent.getEventDefinitions().get(0);
    }
    
    if (eventDefinition instanceof SignalEventDefinition) {
      bpmnParse.getBpmnParserHandlers().parseElement(bpmnParse, eventDefinition);
    } else if (eventDefinition instanceof org.activiti.bpmn.model.CompensateEventDefinition) {
      bpmnParse.getBpmnParserHandlers().parseElement(bpmnParse, eventDefinition);
    } else if (eventDefinition == null) {
      nestedActivityImpl.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createIntermediateThrowNoneEventActivityBehavior(intermediateEvent)); 
    } else { 
      bpmnModel.addProblem("Unsupported intermediate throw event type " + eventDefinition, intermediateEvent);
    }
  }
  
  protected CompensateEventDefinition createCompensateEventDefinition(BpmnParse bpmnParse, org.activiti.bpmn.model.CompensateEventDefinition eventDefinition, ScopeImpl scopeElement) {
    if(StringUtils.isNotEmpty(eventDefinition.getActivityRef())) {
      if(scopeElement.findActivity(eventDefinition.getActivityRef()) == null) {
        bpmnParse.getBpmnModel().addProblem("Invalid attribute value for 'activityRef': no activity with id '" + eventDefinition.getActivityRef() +
            "' in current scope " + scopeElement.getId(), eventDefinition);
      }
    }
    
    CompensateEventDefinition compensateEventDefinition =  new CompensateEventDefinition();
    compensateEventDefinition.setActivityRef(eventDefinition.getActivityRef());
    compensateEventDefinition.setWaitForCompletion(eventDefinition.isWaitForCompletion());
    
    return compensateEventDefinition;
  }

}
