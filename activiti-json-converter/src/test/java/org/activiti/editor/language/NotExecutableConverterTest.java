package org.activiti.editor.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.Test;

public class NotExecutableConverterTest extends AbstractConverterTest {

  @Test
  public void convertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    assertNotNull(bpmnModel);
    validateModel(bpmnModel);
  }

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    assertNotNull(bpmnModel);
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  protected String getResource() {
    return "test.notexecutablemodel.json";
  }

  private void validateModel(BpmnModel model) {
    assertEquals("simpleProcess", model.getMainProcess().getId());
    assertEquals("Simple process", model.getMainProcess().getName());
    assertEquals(false, model.getMainProcess().isExecutable());
  }
}
