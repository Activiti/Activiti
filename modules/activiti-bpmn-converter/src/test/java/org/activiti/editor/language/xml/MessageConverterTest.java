package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Message;
import org.junit.Test;

public class MessageConverterTest extends AbstractConverterTest {
  
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
    Message message = model.getMessage("writeReport");
    assertNotNull(message);
    assertEquals("Examples:writeReportItem", message.getItemRef());
    assertEquals("newWriteReport", message.getName());
    assertEquals("writeReport", message.getId());

    Message message2 = model.getMessage("writeReport2");
    assertNotNull(message2);
    assertEquals("http://foo.bar.com/Examples:writeReportItem2", message2.getItemRef());
    assertEquals("newWriteReport2", message2.getName());
    assertEquals("writeReport2", message2.getId());
  }
  
  protected String getResource() {
        return "message.bpmn";
  }
}