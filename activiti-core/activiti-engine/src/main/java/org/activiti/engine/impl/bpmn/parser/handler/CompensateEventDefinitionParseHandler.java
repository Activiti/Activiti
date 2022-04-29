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
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;

/**


 */
public class CompensateEventDefinitionParseHandler extends AbstractBpmnParseHandler<CompensateEventDefinition> {

  public Class<? extends BaseElement> getHandledType() {
    return CompensateEventDefinition.class;
  }

  protected void executeParse(BpmnParse bpmnParse, CompensateEventDefinition eventDefinition) {

    if (bpmnParse.getCurrentFlowElement() instanceof ThrowEvent) {
      ThrowEvent throwEvent = (ThrowEvent) bpmnParse.getCurrentFlowElement();
      throwEvent.setBehavior(bpmnParse.getActivityBehaviorFactory().createIntermediateThrowCompensationEventActivityBehavior(
          throwEvent, eventDefinition));

    } else if (bpmnParse.getCurrentFlowElement() instanceof BoundaryEvent) {
      BoundaryEvent boundaryEvent = (BoundaryEvent) bpmnParse.getCurrentFlowElement();
      boundaryEvent.setBehavior(bpmnParse.getActivityBehaviorFactory().createBoundaryCompensateEventActivityBehavior(boundaryEvent,
          eventDefinition, boundaryEvent.isCancelActivity()));

    } else {

      // What to do?

    }

  }

}
