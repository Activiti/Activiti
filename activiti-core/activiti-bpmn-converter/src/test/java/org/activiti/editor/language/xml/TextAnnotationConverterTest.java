package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ScriptTask;
import org.junit.jupiter.api.Test;

public class TextAnnotationConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
    deployProcess(parsedModel);
  }

  protected String getResource() {
    return "parsing_error_on_extension_elements.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getFlowElement("_5");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(ScriptTask.class);
    assertThat(flowElement.getId()).isEqualTo("_5");
    ScriptTask scriptTask = (ScriptTask) flowElement;
    assertThat(scriptTask.getId()).isEqualTo("_5");
    assertThat(scriptTask.getName()).isEqualTo("Send Hello Message");
  }
}
