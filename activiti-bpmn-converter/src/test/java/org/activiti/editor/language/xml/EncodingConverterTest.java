package org.activiti.editor.language.xml;

import java.nio.charset.StandardCharsets;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
