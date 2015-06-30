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
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.TerminateEventDefinition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Joram Barrez
 */
public class EndEventParseHandler extends AbstractActivityBpmnParseHandler<EndEvent> {
	
	private static final Logger logger = LoggerFactory.getLogger(EndEventParseHandler.class);
  
  public Class< ? extends BaseElement> getHandledType() {
    return EndEvent.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, EndEvent endEvent) {
    
    ActivityImpl endEventActivity = createActivityOnCurrentScope(bpmnParse, endEvent, BpmnXMLConstants.ELEMENT_EVENT_END);
    EventDefinition eventDefinition = null;
    if (!endEvent.getEventDefinitions().isEmpty()) {
      eventDefinition = endEvent.getEventDefinitions().get(0);
    }
    
    // Error end event
    if (eventDefinition instanceof org.activiti.bpmn.model.ErrorEventDefinition) {
      org.activiti.bpmn.model.ErrorEventDefinition errorDefinition = (org.activiti.bpmn.model.ErrorEventDefinition) eventDefinition;
      if (bpmnParse.getBpmnModel().containsErrorRef(errorDefinition.getErrorCode())) {
        String errorCode = bpmnParse.getBpmnModel().getErrors().get(errorDefinition.getErrorCode());
        if (StringUtils.isEmpty(errorCode)) {
          logger.warn("errorCode is required for an error event " + endEvent.getId());
        }
        endEventActivity.setProperty("type", "errorEndEvent");
        errorDefinition.setErrorCode(errorCode);
      }
      endEventActivity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createErrorEndEventActivityBehavior(endEvent, errorDefinition));
      
    // Cancel end event      
    } else if (eventDefinition instanceof CancelEventDefinition) {
      ScopeImpl scope = bpmnParse.getCurrentScope();
      if (scope.getProperty("type")==null || !scope.getProperty("type").equals("transaction")) {
        logger.warn("end event with cancelEventDefinition only supported inside transaction subprocess (id=" + endEvent.getId() + ")");
      } else {
        endEventActivity.setProperty("type", "cancelEndEvent");
        endEventActivity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createCancelEndEventActivityBehavior(endEvent));
      }
    
    // Terminate end event  
    } else if (eventDefinition instanceof TerminateEventDefinition) {
      endEventActivity.setAsync(endEvent.isAsynchronous());
      endEventActivity.setExclusive(!endEvent.isNotExclusive());
      endEventActivity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createTerminateEndEventActivityBehavior(endEvent));
      
    // None end event  
    } else if (eventDefinition == null) {
      endEventActivity.setAsync(endEvent.isAsynchronous());
      endEventActivity.setExclusive(!endEvent.isNotExclusive());
      endEventActivity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createNoneEndEventActivityBehavior(endEvent));
    }
  }

}
