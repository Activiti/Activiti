package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.junit.Test;

public class CustomExtensionsConverterTest extends AbstractConverterTest {

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
    return "customextensionsmodel.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("servicetask");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof ServiceTask);
    assertEquals("servicetask", flowElement.getId());
    ServiceTask serviceTask = (ServiceTask) flowElement;
    assertEquals("servicetask", serviceTask.getId());
    assertEquals("Service task", serviceTask.getName());
    
    List<FieldExtension> fields = serviceTask.getFieldExtensions();
    assertEquals(2, fields.size());
    FieldExtension field = (FieldExtension) fields.get(0);
    assertEquals("testField", field.getFieldName());
    assertEquals("test", field.getStringValue());
    field = (FieldExtension) fields.get(1);
    assertEquals("testField2", field.getFieldName());
    assertEquals("${test}", field.getExpression());
    
    List<ActivitiListener> listeners = serviceTask.getExecutionListeners();
    assertEquals(3, listeners.size());
    ActivitiListener listener = (ActivitiListener) listeners.get(0);
    assertTrue(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType()));
    assertEquals("org.test.TestClass", listener.getImplementation());
    assertEquals("start", listener.getEvent());
    listener = (ActivitiListener) listeners.get(1);
    assertTrue(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(listener.getImplementationType()));
    assertEquals("${testExpression}", listener.getImplementation());
    assertEquals("end", listener.getEvent());
    listener = (ActivitiListener) listeners.get(2);
    assertTrue(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(listener.getImplementationType()));
    assertEquals("${delegateExpression}", listener.getImplementation());
    assertEquals("start", listener.getEvent());
    
    Map<String, ExtensionElement> extensionElementMap = serviceTask.getExtensionElements();
    assertEquals(1, extensionElementMap.size());
    ExtensionElement extensionElement = extensionElementMap.get("test");
    assertNotNull(extensionElement);
    assertEquals("test", extensionElement.getName());
    assertEquals("custom", extensionElement.getNamespacePrefix());
    assertEquals("http://custom.org/bpmn", extensionElement.getNamespace());
    assertEquals(2, extensionElement.getAttributes().size());
    ExtensionAttribute attribute = extensionElement.getAttributes().get("id");
    assertNotNull(attribute);
    assertEquals("id", attribute.getName());
    assertEquals("test", attribute.getValue());
    assertNull(attribute.getNamespace());
    assertNull(attribute.getNamespacePrefix());
    attribute = extensionElement.getAttributes().get("name");
    assertNotNull(attribute);
    assertEquals("name", attribute.getName());
    assertEquals("test", attribute.getValue());
    assertEquals(2, extensionElement.getChildElements().size());
    ExtensionElement childExtension = extensionElement.getChildElements().get("name");
    assertNotNull(childExtension);
    assertEquals("name", childExtension.getName());
    assertEquals("custom", childExtension.getNamespacePrefix());
    assertEquals("http://custom.org/bpmn", childExtension.getNamespace());
    assertEquals(0, childExtension.getAttributes().size());
    assertEquals(1, childExtension.getChildElements().size());
    childExtension = childExtension.getChildElements().get("test");
    assertNotNull(childExtension);
    assertEquals("test", childExtension.getName());
    assertEquals("custom", childExtension.getNamespacePrefix());
    assertEquals("http://custom.org/bpmn", childExtension.getNamespace());
    assertEquals(0, childExtension.getAttributes().size());
    assertEquals(0, childExtension.getChildElements().size());
    assertEquals("test", childExtension.getElementText());
    childExtension = extensionElement.getChildElements().get("description");
    assertNotNull(childExtension);
    assertEquals("description", childExtension.getName());
    assertEquals(1, childExtension.getAttributes().size());
    attribute = childExtension.getAttributes().get("id");
    assertNotNull(attribute);
    assertEquals("id", attribute.getName());
    assertEquals("test", attribute.getValue());
    assertEquals("custom2", attribute.getNamespacePrefix());
    assertEquals("http://custom2.org/bpmn", attribute.getNamespace());
  }
}
