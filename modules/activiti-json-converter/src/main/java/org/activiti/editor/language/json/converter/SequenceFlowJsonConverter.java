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
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.SequenceFlow;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class SequenceFlowJsonConverter extends BaseBpmnJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    
    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }
  
  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_SEQUENCE_FLOW, SequenceFlowJsonConverter.class);
  }
  
  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(SequenceFlow.class, SequenceFlowJsonConverter.class);
  }
  
  @Override
  protected String getStencilId(FlowElement flowElement) {
    return STENCIL_SEQUENCE_FLOW;
  }
  
  @Override
  public void convertToJson(FlowElement flowElement, ActivityProcessor processor,
      BpmnModel model, ArrayNode shapesArrayNode, double subProcessX, double subProcessY) {
    
    SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
    ObjectNode flowNode = BpmnJsonConverterUtil.createChildShape(sequenceFlow.getId(), STENCIL_SEQUENCE_FLOW, 172, 212, 128, 212);
    ArrayNode dockersArrayNode = objectMapper.createArrayNode();
    ObjectNode dockNode = objectMapper.createObjectNode();
    dockNode.put(EDITOR_BOUNDS_X, model.getGraphicInfo(sequenceFlow.getSourceRef()).getWidth() / 2.0);
    dockNode.put(EDITOR_BOUNDS_Y, model.getGraphicInfo(sequenceFlow.getSourceRef()).getHeight() / 2.0);
    dockersArrayNode.add(dockNode);
    
    if (model.getFlowLocationGraphicInfo(sequenceFlow.getId()).size() > 2) {
      for (int i = 1; i < model.getFlowLocationGraphicInfo(sequenceFlow.getId()).size() - 1; i++) {
        GraphicInfo graphicInfo =  model.getFlowLocationGraphicInfo(sequenceFlow.getId()).get(i);
        dockNode = objectMapper.createObjectNode();
        dockNode.put(EDITOR_BOUNDS_X, graphicInfo.getX());
        dockNode.put(EDITOR_BOUNDS_Y, graphicInfo.getY());
        dockersArrayNode.add(dockNode);
      }
    }
    
    dockNode = objectMapper.createObjectNode();
    dockNode.put(EDITOR_BOUNDS_X, model.getGraphicInfo(sequenceFlow.getTargetRef()).getWidth() / 2.0);
    dockNode.put(EDITOR_BOUNDS_Y, model.getGraphicInfo(sequenceFlow.getTargetRef()).getHeight() / 2.0);
    dockersArrayNode.add(dockNode);
    flowNode.put("dockers", dockersArrayNode);
    ArrayNode outgoingArrayNode = objectMapper.createArrayNode();
    outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(sequenceFlow.getTargetRef()));
    flowNode.put("outgoing", outgoingArrayNode);
    flowNode.put("target", BpmnJsonConverterUtil.createResourceNode(sequenceFlow.getTargetRef()));
    
    ObjectNode propertiesNode = objectMapper.createObjectNode();
    propertiesNode.put(PROPERTY_OVERRIDE_ID, flowElement.getId());
    if (StringUtils.isNotEmpty(sequenceFlow.getName())) {
      propertiesNode.put(PROPERTY_NAME, sequenceFlow.getName());
    }
    
    if (StringUtils.isNotEmpty(sequenceFlow.getDocumentation())) {
      propertiesNode.put(PROPERTY_DOCUMENTATION, sequenceFlow.getDocumentation());
    }
    
    if (StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())) {
      propertiesNode.put(PROPERTY_SEQUENCEFLOW_CONDITION, sequenceFlow.getConditionExpression());
    }
    
    flowNode.put(EDITOR_SHAPE_PROPERTIES, propertiesNode);
    
    shapesArrayNode.add(flowNode);
  }
  
  @Override
  protected void convertElementToJson(ObjectNode propertiesNode, FlowElement flowElement) {
    // nothing to do
  }
  
  @Override
  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    SequenceFlow flow = new SequenceFlow();
    
    String sourceRef = lookForSourceRef(elementNode.get(EDITOR_SHAPE_ID).asText(), modelNode.get(EDITOR_CHILD_SHAPES));
    
    if (sourceRef != null) {
      flow.setSourceRef(sourceRef);
      String targetId = elementNode.get("target").get(EDITOR_SHAPE_ID).asText();
      flow.setTargetRef(BpmnJsonConverterUtil.getElementId(shapeMap.get(targetId)));
    }
    
    flow.setConditionExpression(getPropertyValueAsString(PROPERTY_SEQUENCEFLOW_CONDITION, elementNode));
    
    return flow;
  }
  
  private String lookForSourceRef(String flowId, JsonNode childShapesNode) {
    String sourceRef = null;
    
    if (childShapesNode != null) {
    
      for (JsonNode childNode : childShapesNode) {
        ArrayNode outgoingNode = (ArrayNode) childNode.get("outgoing");
        if (outgoingNode != null && outgoingNode.size() > 0) {
          for (JsonNode outgoingChildNode : outgoingNode) {
            JsonNode resourceNode = outgoingChildNode.get(EDITOR_SHAPE_ID);
            if (resourceNode != null && flowId.equals(resourceNode.asText())) {
              sourceRef = BpmnJsonConverterUtil.getElementId(childNode);
              break;
            }
          }
          
          if (sourceRef != null) {
            break;
          }
        }
        sourceRef = lookForSourceRef(flowId, childNode.get(EDITOR_CHILD_SHAPES));
        
        if (sourceRef != null) {
          break;
        }
      }
    }
    return sourceRef;
  }
}
