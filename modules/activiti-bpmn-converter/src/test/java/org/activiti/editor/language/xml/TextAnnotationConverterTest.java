package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ScriptTask;
import org.junit.Test;

public class TextAnnotationConverterTest extends AbstractConverterTest {

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
    return "parsing_error_on_extension_elements.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getFlowElement("_5");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof ScriptTask);
    assertEquals("_5", flowElement.getId());
    ScriptTask scriptTask = (ScriptTask) flowElement;
    assertEquals("_5", scriptTask.getId());
    assertEquals("Send Hello Message", scriptTask.getName());
  }
}
