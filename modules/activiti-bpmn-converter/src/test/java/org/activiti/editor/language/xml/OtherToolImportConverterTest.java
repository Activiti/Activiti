package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.Test;

public class OtherToolImportConverterTest extends AbstractConverterTest {

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
    return "othertoolimport.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    org.activiti.bpmn.model.Process process = model.getProcess("_GQ4P0PUQEeK4teimjV5_yg");
    assertNotNull(process);
    assertEquals("Carpet_Plus", process.getId());
    assertEquals("Carpet-Plus", process.getName());
    assertTrue(process.isExecutable());
  }
}
