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
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Error;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.TerminateEventDefinition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 */
public class EndEventParseHandler extends AbstractActivityBpmnParseHandler<EndEvent> {

  private static final Logger logger = LoggerFactory.getLogger(EndEventParseHandler.class);

  public Class<? extends BaseElement> getHandledType() {
    return EndEvent.class;
  }

  @Override
  protected void executeParse(BpmnParse bpmnParse, EndEvent endEvent) {

    EventDefinition eventDefinition = null;
    if (endEvent.getEventDefinitions().size() > 0) {
      eventDefinition = endEvent.getEventDefinitions().get(0);

      if (eventDefinition instanceof ErrorEventDefinition) {
        ErrorEventDefinition errorDefinition = (ErrorEventDefinition) eventDefinition;
        if (bpmnParse.getBpmnModel().containsErrorRef(errorDefinition.getErrorRef())) {

          for(Error error : bpmnParse.getBpmnModel().getErrors().values()) {
            String errorCode = null;
            if(error.getId().equals(errorDefinition.getErrorRef())){
              errorCode = error.getErrorCode();
            }
            if (StringUtils.isEmpty(errorCode)) {
              logger.warn("errorCode is required for an error event " + endEvent.getId());
            }
          }
        }
        endEvent.setBehavior(bpmnParse.getActivityBehaviorFactory().createErrorEndEventActivityBehavior(endEvent, errorDefinition));
      } else if (eventDefinition instanceof TerminateEventDefinition) {
        endEvent.setBehavior(bpmnParse.getActivityBehaviorFactory().createTerminateEndEventActivityBehavior(endEvent));
      } else if (eventDefinition instanceof CancelEventDefinition) {
        endEvent.setBehavior(bpmnParse.getActivityBehaviorFactory().createCancelEndEventActivityBehavior(endEvent));
      } else if (eventDefinition instanceof MessageEventDefinition) {
        MessageEventDefinition messageEventDefinition = MessageEventDefinition.class
                                                                              .cast(eventDefinition);
        Message message = bpmnParse.getBpmnModel()
                                   .getMessage(messageEventDefinition.getMessageRef());
        
        BpmnModel bpmnModel = bpmnParse.getBpmnModel();
        if (bpmnModel.containsMessageId(messageEventDefinition.getMessageRef())) {
          messageEventDefinition.setMessageRef(message.getName());
          messageEventDefinition.setExtensionElements(message.getExtensionElements());
        }
          
        endEvent.setBehavior(bpmnParse.getActivityBehaviorFactory()
                                      .createThrowMessageEndEventActivityBehavior(endEvent, 
                                                                                  messageEventDefinition, 
                                                                                  message));
      } else {
        endEvent.setBehavior(bpmnParse.getActivityBehaviorFactory().createNoneEndEventActivityBehavior(endEvent));
      }

    } else {
      endEvent.setBehavior(bpmnParse.getActivityBehaviorFactory().createNoneEndEventActivityBehavior(endEvent));
    }
  }

}
