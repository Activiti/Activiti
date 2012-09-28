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
package org.activiti.editor.language.json.converter;

import org.activiti.editor.language.bpmn.model.ErrorEventDefinition;
import org.activiti.editor.language.bpmn.model.Event;
import org.activiti.editor.language.bpmn.model.EventDefinition;
import org.activiti.editor.language.bpmn.model.MessageEventDefinition;
import org.activiti.editor.language.bpmn.model.SignalEventDefinition;
import org.activiti.editor.language.bpmn.model.StartEvent;
import org.activiti.editor.language.bpmn.model.TimerEventDefinition;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class StartEventConverter extends BaseBpmnElementToJsonConverter {

  protected String getActivityType() {
    Event event = (Event) flowElement;
    if (event.getEventDefinitions().size() > 0) {
      EventDefinition eventDefinition = event.getEventDefinitions().get(0);
      if (eventDefinition instanceof TimerEventDefinition) {
        return STENCIL_EVENT_START_TIMER;
      } else if (eventDefinition instanceof ErrorEventDefinition) {
        return STENCIL_EVENT_START_ERROR;
      } else if (eventDefinition instanceof MessageEventDefinition) {
        return STENCIL_EVENT_START_MESSAGE;
      } else if (eventDefinition instanceof SignalEventDefinition) {
        return STENCIL_EVENT_START_SIGNAL;
      } 
    }
    return STENCIL_EVENT_START_NONE;
  }
  
  protected void convertElement(ObjectNode propertiesNode) {
    StartEvent startEvent = (StartEvent) flowElement;
    if (StringUtils.isNotEmpty(startEvent.getInitiator())) {
    	propertiesNode.put(PROPERTY_NONE_STARTEVENT_INITIATOR, startEvent.getInitiator());
    }
    addFormProperties(startEvent.getFormProperties(), propertiesNode);
    addEventProperties(startEvent, propertiesNode);
  }
}
