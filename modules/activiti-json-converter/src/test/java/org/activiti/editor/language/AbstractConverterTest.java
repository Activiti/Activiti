package org.activiti.editor.language;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.InputStream;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public abstract class AbstractConverterTest {

  protected BpmnModel readJsonFile() throws Exception {
    InputStream jsonStream = this.getClass().getClassLoader().getResourceAsStream(getResource());
    JsonNode modelNode = new ObjectMapper().readTree(jsonStream);
    BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
    return bpmnModel;
  }
  
  protected BpmnModel convertToJsonAndBack(BpmnModel bpmnModel) {
    ObjectNode modelNode = new BpmnJsonConverter().convertToJson(bpmnModel);
    System.out.println("JSON: " + modelNode.toString());
    bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
    return bpmnModel;
  }
  
  protected EventDefinition extractEventDefinition(FlowElement flowElement) {
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof Event);
    Event event = (Event)flowElement;
    assertFalse(event.getEventDefinitions().isEmpty());
    return event.getEventDefinitions().get(0);
  }
  
  protected abstract String getResource();
}
