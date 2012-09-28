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

import java.util.List;

import org.activiti.editor.language.bpmn.model.EventDefinition;
import org.activiti.editor.language.bpmn.model.IntermediateCatchEvent;
import org.activiti.editor.language.bpmn.model.MessageEventDefinition;
import org.activiti.editor.language.bpmn.model.SignalEventDefinition;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class CatchEventConverter extends BaseBpmnElementToJsonConverter {
  
  protected String getActivityType() {
    IntermediateCatchEvent catchEvent = (IntermediateCatchEvent) flowElement;
    List<EventDefinition> eventDefinitions = catchEvent.getEventDefinitions();
    if (eventDefinitions.size() != 1) {
      // return timer event as default;
      return STENCIL_EVENT_CATCH_TIMER;
    }
    
    EventDefinition eventDefinition = eventDefinitions.get(0);
    if (eventDefinition instanceof MessageEventDefinition) {
      return STENCIL_EVENT_CATCH_MESSAGE;
    } else if (eventDefinition instanceof SignalEventDefinition) {
      return STENCIL_EVENT_CATCH_SIGNAL;
    } else {
      return STENCIL_EVENT_CATCH_TIMER;
    }
  }

  protected void convertElement(ObjectNode propertiesNode) {
    IntermediateCatchEvent catchEvent = (IntermediateCatchEvent) flowElement;
    addEventProperties(catchEvent, propertiesNode);
  }
}
