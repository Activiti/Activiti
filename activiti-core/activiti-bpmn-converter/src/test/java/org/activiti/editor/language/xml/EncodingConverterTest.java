package org.activiti.editor.language.xml;

import java.nio.charset.StandardCharsets;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EncodingConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFileEncoding(StandardCharsets.ISO_8859_1.name());
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFileEncoding(StandardCharsets.ISO_8859_1.name());
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("writeReportTask");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(UserTask.class);
    assertThat(flowElement.getId()).isEqualTo("writeReportTask");
    UserTask userTask = (UserTask) flowElement;
    assertThat(userTask.getId()).isEqualTo("writeReportTask");
    assertThat(userTask.getName()).isEqualTo("Fazer relatório");
  }

  protected String getResource() {
    return "encoding.bpmn";
  }
}
