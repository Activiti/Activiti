package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.jupiter.api.Test;

public class NotExecutableConverterTest extends AbstractConverterTest {

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
    return "notexecutablemodel.bpmn";
  }

  private void validateModel(BpmnModel model) {
    assertThat(model.getMainProcess().getId()).isEqualTo("simpleProcess");
    assertThat(model.getMainProcess().getName()).isEqualTo("Simple process");
    assertThat(model.getMainProcess().isExecutable()).isEqualTo(false);
  }
}
