package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.jupiter.api.Test;

public class NotExecutableConverterTest extends AbstractConverterTest {

  @Test
  public void convertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  protected String getResource() {
    return "test.notexecutablemodel.json";
  }

  private void validateModel(BpmnModel model) {
    assertThat(model.getMainProcess().getId()).isEqualTo("simpleProcess");
    assertThat(model.getMainProcess().getName()).isEqualTo("Simple process");
    assertThat(model.getMainProcess().isExecutable()).isEqualTo(false);
  }
}
