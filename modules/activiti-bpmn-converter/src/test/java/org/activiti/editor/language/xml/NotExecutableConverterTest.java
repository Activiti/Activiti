package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.Test;

public class NotExecutableConverterTest extends AbstractConverterTest {

  @Test
  public void connvertXMLToModel() throws Exception {
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
    assertEquals("simpleProcess", model.getMainProcess().getId());
    assertEquals("Simple process", model.getMainProcess().getName());
    assertEquals(false, model.getMainProcess().isExecutable());
  }
}
