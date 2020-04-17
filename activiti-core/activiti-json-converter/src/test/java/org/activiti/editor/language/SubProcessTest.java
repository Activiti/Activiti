package org.activiti.editor.language;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SubProcessTest extends AbstractConverterTest {

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readXmlFile();
    validateModel(bpmnModel);
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  private void validateModel(BpmnModel model) {
    assertThat(10).isEqualTo(model.getMainProcess().getFlowElementMap().keySet().size());
  }

  protected String getResource() {
    return "test.subprocess.xml";
  }

}
