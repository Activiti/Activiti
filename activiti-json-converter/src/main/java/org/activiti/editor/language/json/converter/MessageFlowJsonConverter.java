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

import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.MessageFlow;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**

 */
public class MessageFlowJsonConverter extends BaseBpmnJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }

  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_MESSAGE_FLOW, MessageFlowJsonConverter.class);
  }

  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(MessageFlow.class, MessageFlowJsonConverter.class);
  }

  @Override
  protected String getStencilId(BaseElement baseElement) {
    return STENCIL_MESSAGE_FLOW;
  }

  @Override
  public void convertToJson(BaseElement baseElement, ActivityProcessor processor, BpmnModel model, FlowElementsContainer container, ArrayNode shapesArrayNode, double subProcessX, double subProcessY) {

    MessageFlow messageFlow = (MessageFlow) baseElement;
    ObjectNode flowNode = BpmnJsonConverterUtil.createChildShape(messageFlow.getId(), STENCIL_MESSAGE_FLOW, 172, 212, 128, 212);
    ArrayNode dockersArrayNode = objectMapper.createArrayNode();
    ObjectNode dockNode = objectMapper.createObjectNode();
    dockNode.put(EDITOR_BOUNDS_X, model.getGraphicInfo(messageFlow.getSourceRef()).getWidth() / 2.0);
    dockNode.put(EDITOR_BOUNDS_Y, model.getGraphicInfo(messageFlow.getSourceRef()).getHeight() / 2.0);
    dockersArrayNode.add(dockNode);

    if (model.getFlowLocationGraphicInfo(messageFlow.getId()).size() > 2) {
      for (int i = 1; i < model.getFlowLocationGraphicInfo(messageFlow.getId()).size() - 1; i++) {
        GraphicInfo graphicInfo = model.getFlowLocationGraphicInfo(messageFlow.getId()).get(i);
        dockNode = objectMapper.createObjectNode();
        dockNode.put(EDITOR_BOUNDS_X, graphicInfo.getX());
        dockNode.put(EDITOR_BOUNDS_Y, graphicInfo.getY());
        dockersArrayNode.add(dockNode);
      }
    }

    dockNode = objectMapper.createObjectNode();
    dockNode.put(EDITOR_BOUNDS_X, model.getGraphicInfo(messageFlow.getTargetRef()).getWidth() / 2.0);
    dockNode.put(EDITOR_BOUNDS_Y, model.getGraphicInfo(messageFlow.getTargetRef()).getHeight() / 2.0);
    dockersArrayNode.add(dockNode);
    flowNode.set("dockers", dockersArrayNode);
    ArrayNode outgoingArrayNode = objectMapper.createArrayNode();
    outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(messageFlow.getTargetRef()));
    flowNode.set("outgoing", outgoingArrayNode);
    flowNode.set("target", BpmnJsonConverterUtil.createResourceNode(messageFlow.getTargetRef()));

    ObjectNode propertiesNode = objectMapper.createObjectNode();
    propertiesNode.put(PROPERTY_OVERRIDE_ID, messageFlow.getId());
    if (StringUtils.isNotEmpty(messageFlow.getName())) {
      propertiesNode.put(PROPERTY_NAME, messageFlow.getName());
    }

    flowNode.set(EDITOR_SHAPE_PROPERTIES, propertiesNode);
    shapesArrayNode.add(flowNode);
  }

  @Override
  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
    // nothing to do
  }

  @Override
  protected BaseElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    MessageFlow flow = new MessageFlow();

    String sourceRef = BpmnJsonConverterUtil.lookForSourceRef(elementNode.get(EDITOR_SHAPE_ID).asText(), modelNode.get(EDITOR_CHILD_SHAPES));
    if (sourceRef != null) {
      flow.setSourceRef(sourceRef);
      JsonNode targetNode = elementNode.get("target");
      if (targetNode != null && !targetNode.isNull()) {
        String targetId = targetNode.get(EDITOR_SHAPE_ID).asText();
        if (shapeMap.get(targetId) != null) {
          flow.setTargetRef(BpmnJsonConverterUtil.getElementId(shapeMap.get(targetId)));
        }
      }
    }

    return flow;
  }
}
