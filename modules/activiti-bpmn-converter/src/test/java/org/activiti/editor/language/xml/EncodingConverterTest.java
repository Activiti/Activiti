package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class EncodingConverterTest extends AbstractConverterTest {
  
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
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("writeReportTask");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof UserTask);
    assertEquals("writeReportTask", flowElement.getId());
    UserTask userTask = (UserTask) flowElement;
    assertEquals("writeReportTask", userTask.getId());
    assertEquals("Fazer relatório", userTask.getName());
  }
  
  protected String getResource() {
    return "encoding.bpmn";
  }
}
