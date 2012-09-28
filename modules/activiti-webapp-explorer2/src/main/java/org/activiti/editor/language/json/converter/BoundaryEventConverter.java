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

import org.activiti.editor.language.bpmn.model.BoundaryEvent;
import org.activiti.editor.language.bpmn.model.ErrorEventDefinition;
import org.activiti.editor.language.bpmn.model.EventDefinition;
import org.activiti.editor.language.bpmn.model.SignalEventDefinition;
import org.activiti.editor.language.bpmn.parser.GraphicInfo;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class BoundaryEventConverter extends BaseBpmnElementToJsonConverter {
  
  protected String getActivityType() {
    BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
    List<EventDefinition> eventDefinitions = boundaryEvent.getEventDefinitions();
    if (eventDefinitions.size() != 1) {
      // return timer event as default;
      return STENCIL_EVENT_BOUNDARY_TIMER;
    }
    
    EventDefinition eventDefinition = eventDefinitions.get(0);
    if (eventDefinition instanceof ErrorEventDefinition) {
      return STENCIL_EVENT_BOUNDARY_ERROR;
    } else if (eventDefinition instanceof SignalEventDefinition) {
      return STENCIL_EVENT_BOUNDARY_SIGNAL;
    } else {
      return STENCIL_EVENT_BOUNDARY_TIMER;
    }
  }

  protected void convertElement(ObjectNode propertiesNode) {
    BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
    ArrayNode dockersArrayNode = objectMapper.createArrayNode();
    ObjectNode dockNode = objectMapper.createObjectNode();
    GraphicInfo graphicInfo = model.getGraphicInfo(boundaryEvent.getId());
    GraphicInfo parentGraphicInfo = model.getGraphicInfo(boundaryEvent.getAttachedToRef().getId());
    dockNode.put(EDITOR_BOUNDS_X, graphicInfo.x + graphicInfo.width - parentGraphicInfo.x);
    dockNode.put(EDITOR_BOUNDS_Y, graphicInfo.y - parentGraphicInfo.y);
    dockersArrayNode.add(dockNode);
    flowElementNode.put("dockers", dockersArrayNode);
    
    addEventProperties(boundaryEvent, propertiesNode);
  }
}
