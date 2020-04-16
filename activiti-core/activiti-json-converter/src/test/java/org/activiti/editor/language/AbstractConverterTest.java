package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
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

  protected BpmnModel readXmlFile() throws Exception {
    final InputStream jsonStream = this.getClass().getClassLoader().getResourceAsStream(getResource());
    return new BpmnXMLConverter().convertToBpmnModel(new InputStreamProvider() {
      @Override
      public InputStream getInputStream() {
        return jsonStream;
      }
    }, false, false);
  }

  protected BpmnModel convertToJsonAndBack(BpmnModel bpmnModel) {
    ObjectNode modelNode = new BpmnJsonConverter().convertToJson(bpmnModel);
    bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
    return bpmnModel;
  }

  protected EventDefinition extractEventDefinition(FlowElement flowElement) {
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(Event.class);
    Event event = (Event) flowElement;
    assertThat(event.getEventDefinitions().isEmpty()).isFalse();
    return event.getEventDefinitions().get(0);
  }

  protected abstract String getResource();
}
