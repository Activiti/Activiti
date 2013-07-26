package org.activiti.editor.language;

import java.io.InputStream;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractConverterTest {

  protected BpmnModel readJsonFile() throws Exception {
    InputStream jsonStream = this.getClass().getClassLoader().getResourceAsStream(getResource());
    JsonNode modelNode = new ObjectMapper().readTree(jsonStream);
    BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
    return bpmnModel;
  }
  
  protected BpmnModel convertToJsonAndBack(BpmnModel bpmnModel) {
    ObjectNode modelNode = new BpmnJsonConverter().convertToJson(bpmnModel);
    bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
    return bpmnModel;
  }
  
  protected abstract String getResource();
}
