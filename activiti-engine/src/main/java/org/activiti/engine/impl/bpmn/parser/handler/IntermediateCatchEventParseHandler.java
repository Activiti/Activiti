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
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 */
public class IntermediateCatchEventParseHandler extends AbstractFlowNodeBpmnParseHandler<IntermediateCatchEvent> {

  private static final Logger logger = LoggerFactory.getLogger(IntermediateCatchEventParseHandler.class);

  public Class<? extends BaseElement> getHandledType() {
    return IntermediateCatchEvent.class;
  }

  protected void executeParse(BpmnParse bpmnParse, IntermediateCatchEvent event) {
    EventDefinition eventDefinition = null;
    if (!event.getEventDefinitions().isEmpty()) {
      eventDefinition = event.getEventDefinitions().get(0);
    }

    if (eventDefinition == null) {
      event.setBehavior(bpmnParse.getActivityBehaviorFactory().createIntermediateCatchEventActivityBehavior(event));

    } else {
      if (eventDefinition instanceof TimerEventDefinition || eventDefinition instanceof SignalEventDefinition || eventDefinition instanceof MessageEventDefinition) {

        bpmnParse.getBpmnParserHandlers().parseElement(bpmnParse, eventDefinition);

      } else {
        logger.warn("Unsupported intermediate catch event type for event " + event.getId());
      }
    }
  }

}
