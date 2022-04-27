/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.editor.language.json.converter;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class BoundaryEventJsonConverter extends BaseBpmnJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }

  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_EVENT_BOUNDARY_TIMER, BoundaryEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_BOUNDARY_ERROR, BoundaryEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_BOUNDARY_SIGNAL, BoundaryEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_BOUNDARY_MESSAGE, BoundaryEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_BOUNDARY_CANCEL, BoundaryEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_BOUNDARY_COMPENSATION, BoundaryEventJsonConverter.class);
  }

  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(BoundaryEvent.class, BoundaryEventJsonConverter.class);
  }

  protected String getStencilId(BaseElement baseElement) {
    BoundaryEvent boundaryEvent = (BoundaryEvent) baseElement;
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
    } else if (eventDefinition instanceof MessageEventDefinition) {
      return STENCIL_EVENT_BOUNDARY_MESSAGE;
    } else if (eventDefinition instanceof CancelEventDefinition) {
      return STENCIL_EVENT_BOUNDARY_CANCEL;
    } else if (eventDefinition instanceof CompensateEventDefinition) {
      return STENCIL_EVENT_BOUNDARY_COMPENSATION;
    } else {
      return STENCIL_EVENT_BOUNDARY_TIMER;
    }
  }

  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
    BoundaryEvent boundaryEvent = (BoundaryEvent) baseElement;
    ArrayNode dockersArrayNode = objectMapper.createArrayNode();
    ObjectNode dockNode = objectMapper.createObjectNode();
    GraphicInfo graphicInfo = model.getGraphicInfo(boundaryEvent.getId());
    GraphicInfo parentGraphicInfo = model.getGraphicInfo(boundaryEvent.getAttachedToRef().getId());
    dockNode.put(EDITOR_BOUNDS_X, graphicInfo.getX() - parentGraphicInfo.getX());
    dockNode.put(EDITOR_BOUNDS_Y, graphicInfo.getY() - parentGraphicInfo.getY());
    dockersArrayNode.add(dockNode);
    flowElementNode.set("dockers", dockersArrayNode);

    propertiesNode.put(PROPERTY_CANCEL_ACTIVITY, boundaryEvent.isCancelActivity());

    addEventProperties(boundaryEvent, propertiesNode);
  }

  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    BoundaryEvent boundaryEvent = new BoundaryEvent();
    String stencilId = BpmnJsonConverterUtil.getStencilId(elementNode);
    if (STENCIL_EVENT_BOUNDARY_TIMER.equals(stencilId)) {
      convertJsonToTimerDefinition(elementNode, boundaryEvent);
      boundaryEvent.setCancelActivity(getPropertyValueAsBoolean(PROPERTY_CANCEL_ACTIVITY, elementNode));

    } else if (STENCIL_EVENT_BOUNDARY_ERROR.equals(stencilId)) {
      convertJsonToErrorDefinition(elementNode, boundaryEvent);

    } else if (STENCIL_EVENT_BOUNDARY_SIGNAL.equals(stencilId)) {
      convertJsonToSignalDefinition(elementNode, boundaryEvent);
      boundaryEvent.setCancelActivity(getPropertyValueAsBoolean(PROPERTY_CANCEL_ACTIVITY, elementNode));

    } else if (STENCIL_EVENT_BOUNDARY_MESSAGE.equals(stencilId)) {
      convertJsonToMessageDefinition(elementNode, boundaryEvent);
      boundaryEvent.setCancelActivity(getPropertyValueAsBoolean(PROPERTY_CANCEL_ACTIVITY, elementNode));

    } else if (STENCIL_EVENT_BOUNDARY_CANCEL.equals(stencilId)) {
      CancelEventDefinition cancelEventDefinition = new CancelEventDefinition();
      boundaryEvent.getEventDefinitions().add(cancelEventDefinition);
      boundaryEvent.setCancelActivity(getPropertyValueAsBoolean(PROPERTY_CANCEL_ACTIVITY, elementNode));

    } else if (STENCIL_EVENT_BOUNDARY_COMPENSATION.equals(stencilId)) {
      CompensateEventDefinition compensateEventDefinition = new CompensateEventDefinition();
      boundaryEvent.getEventDefinitions().add(compensateEventDefinition);
      boundaryEvent.setCancelActivity(getPropertyValueAsBoolean(PROPERTY_CANCEL_ACTIVITY, elementNode));
    }
    boundaryEvent.setAttachedToRefId(lookForAttachedRef(elementNode.get(EDITOR_SHAPE_ID).asText(), modelNode.get(EDITOR_CHILD_SHAPES)));
    return boundaryEvent;
  }

  private String lookForAttachedRef(String boundaryEventId, JsonNode childShapesNode) {
    String attachedRefId = null;

    if (childShapesNode != null) {

      for (JsonNode childNode : childShapesNode) {
        ArrayNode outgoingNode = (ArrayNode) childNode.get("outgoing");
        if (outgoingNode != null && outgoingNode.size() > 0) {
          for (JsonNode outgoingChildNode : outgoingNode) {
            JsonNode resourceNode = outgoingChildNode.get(EDITOR_SHAPE_ID);
            if (resourceNode != null && boundaryEventId.equals(resourceNode.asText())) {
              attachedRefId = BpmnJsonConverterUtil.getElementId(childNode);
              break;
            }
          }

          if (attachedRefId != null) {
            break;
          }
        }

        attachedRefId = lookForAttachedRef(boundaryEventId, childNode.get(EDITOR_CHILD_SHAPES));

        if (attachedRefId != null) {
          break;
        }
      }
    }

    return attachedRefId;
  }
}
