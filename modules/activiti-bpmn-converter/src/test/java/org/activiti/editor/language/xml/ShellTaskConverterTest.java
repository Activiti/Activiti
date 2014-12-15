package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ServiceTask;
import org.junit.Test;

public class ShellTaskConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
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
    return "shelltaskmodel.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("servicetask");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof ServiceTask);
    assertEquals("servicetask", flowElement.getId());
    ServiceTask serviceTask = (ServiceTask) flowElement;
    assertEquals("servicetask", serviceTask.getId());
    assertEquals("Shell task", serviceTask.getName());
    assertEquals("shell", serviceTask.getType());
    
    List<FieldExtension> fields = serviceTask.getFieldExtensions();
    assertEquals(6, fields.size());
    FieldExtension field = (FieldExtension) fields.get(0);
    assertEquals("command", field.getFieldName());
    assertEquals("cmd", field.getStringValue());
    field = (FieldExtension) fields.get(1);
    assertEquals("arg1", field.getFieldName());
    assertEquals("/c", field.getStringValue());
    field = (FieldExtension) fields.get(2);
    assertEquals("arg2", field.getFieldName());
    assertEquals("echo", field.getStringValue());
    field = (FieldExtension) fields.get(3);
    assertEquals("arg3", field.getFieldName());
    assertEquals("EchoTest", field.getStringValue());
    field = (FieldExtension) fields.get(4);
    assertEquals("wait", field.getFieldName());
    assertEquals("true", field.getStringValue());
    field = (FieldExtension) fields.get(5);
    assertEquals("outputVariable", field.getFieldName());
    assertEquals("resultVar", field.getStringValue());
  }
}
