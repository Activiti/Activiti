package org.activiti.editor.language.json.converter;

import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MessageFlow;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class MessageFlowJsonConverter extends BaseFlowBpmnJsonConverter {

	  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
	      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
	    
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
	  protected String getStencilId(FlowElement flowElement) {
	    return STENCIL_MESSAGE_FLOW;
	  }
	  
	  @Override
	  public void convertToJson(FlowElement flowElement, ActivityProcessor processor,
	      BpmnModel model, ArrayNode shapesArrayNode, double subProcessX, double subProcessY) {
	    
	    MessageFlow messageFlow = (MessageFlow) flowElement;
	    ObjectNode flowNode = BpmnJsonConverterUtil.createChildShape(messageFlow.getId(), STENCIL_MESSAGE_FLOW, 172, 212, 128, 212);
	    
	    getFlowGraphicInfo(messageFlow, flowNode, model, subProcessX, subProcessY);
	    
	    ArrayNode outgoingArrayNode = objectMapper.createArrayNode();
	    outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(messageFlow.getTargetRef()));
	    flowNode.put("outgoing", outgoingArrayNode);
	    flowNode.put("target", BpmnJsonConverterUtil.createResourceNode(messageFlow.getTargetRef()));
	    
	    ObjectNode propertiesNode = objectMapper.createObjectNode();
	    propertiesNode.put(PROPERTY_OVERRIDE_ID, flowElement.getId());
	    if (StringUtils.isNotEmpty(messageFlow.getName())) {
	      propertiesNode.put(PROPERTY_NAME, messageFlow.getName());
	    }
	    
	    if (StringUtils.isNotEmpty(messageFlow.getDocumentation())) {
	      propertiesNode.put(PROPERTY_DOCUMENTATION, messageFlow.getDocumentation());
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
	    MessageFlow flow = new MessageFlow();
	    
	    String sourceRef = lookForSourceRef(elementNode.get(EDITOR_SHAPE_ID).asText(), modelNode.get(EDITOR_CHILD_SHAPES));
	    
	    if (sourceRef != null) {
	      flow.setSourceRef(sourceRef);
	      String targetId = elementNode.get("target").get(EDITOR_SHAPE_ID).asText();
	      flow.setTargetRef(BpmnJsonConverterUtil.getElementId(shapeMap.get(targetId)));
	    }
	    
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

