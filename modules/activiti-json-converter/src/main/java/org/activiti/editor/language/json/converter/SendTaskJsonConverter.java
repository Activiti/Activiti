package org.activiti.editor.language.json.converter;

import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SendTask;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

public class SendTaskJsonConverter extends BaseBpmnJsonConverter {

	  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
	      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
	    
	    fillJsonTypes(convertersToBpmnMap);
	    fillBpmnTypes(convertersToJsonMap);
	  }
	  
	  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
	    convertersToBpmnMap.put(STENCIL_TASK_SEND, SendTaskJsonConverter.class);
	  }
	  
	  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
	    convertersToJsonMap.put(SendTask.class, SendTaskJsonConverter.class);
	  }
	  
	  protected String getStencilId(FlowElement flowElement) {
	    return STENCIL_TASK_SEND;
	  }
	  
	  protected void convertElementToJson(ObjectNode propertiesNode, FlowElement flowElement) {
	  	
	  }
	  
	  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
	    SendTask task = new SendTask();
	    return task;
	  }
	}
