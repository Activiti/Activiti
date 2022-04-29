/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 */
public class BoundaryEventParseHandler extends AbstractFlowNodeBpmnParseHandler<BoundaryEvent> {

  private static final Logger logger = LoggerFactory.getLogger(BoundaryEventParseHandler.class);

  public Class<? extends BaseElement> getHandledType() {
    return BoundaryEvent.class;
  }

  protected void executeParse(BpmnParse bpmnParse, BoundaryEvent boundaryEvent) {

    if (boundaryEvent.getAttachedToRef() == null) {
      logger.warn("Invalid reference in boundary event. Make sure that the referenced activity " + "is defined in the same scope as the boundary event " + boundaryEvent.getId());
      return;
    }

    EventDefinition eventDefinition = null;
    if (boundaryEvent.getEventDefinitions().size() > 0) {
      eventDefinition = boundaryEvent.getEventDefinitions().get(0);
    }

    if (eventDefinition instanceof TimerEventDefinition || eventDefinition instanceof ErrorEventDefinition || eventDefinition instanceof SignalEventDefinition
        || eventDefinition instanceof CancelEventDefinition || eventDefinition instanceof MessageEventDefinition || eventDefinition instanceof CompensateEventDefinition) {

      bpmnParse.getBpmnParserHandlers().parseElement(bpmnParse, eventDefinition);

    } else {
      // Should already be picked up by process validator on deploy, so this is just to be sure
      logger.warn("Unsupported boundary event type for boundary event " + boundaryEvent.getId());
    }

  }

}
