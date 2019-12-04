package org.activiti.editor.language;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SubProcessTest extends AbstractConverterTest {

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readXmlFile();
    validateModel(bpmnModel);
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  private void validateModel(BpmnModel model) {
    assertEquals(model.getMainProcess().getFlowElementMap().keySet().size(), 10);
  }

  protected String getResource() {
    return "test.subprocess.xml";
  }

}
