package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.junit.Test;

public class CustomExtensionsConverterTest extends AbstractConverterTest {

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
    return "customextensionsmodel.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    Process process = model.getMainProcess();
    assertNotNull(process.getAttributes());
    assertEquals(1, process.getAttributes().size());
    List<ExtensionAttribute> attributes = process.getAttributes().get("version");
    assertNotNull(attributes);
    assertEquals(1, attributes.size());
    ExtensionAttribute attribute = attributes.get(0);
    //custom:version = "9"
    assertNotNull(attribute);
    assertEquals("http://custom.org/bpmn", attribute.getNamespace());
    assertEquals("custom", attribute.getNamespacePrefix());
    assertEquals("version", attribute.getName());
    assertEquals("9", attribute.getValue());

    List<ActivitiListener> listeners = model.getMainProcess().getExecutionListeners();
    validateExecutionListeners(listeners);
    Map<String, List<ExtensionElement>> extensionElementMap = model.getMainProcess().getExtensionElements();
    validateExtensionElements(extensionElementMap);
    
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
    
    listeners = serviceTask.getExecutionListeners();
    validateExecutionListeners(listeners);
    
    extensionElementMap = serviceTask.getExtensionElements();
    validateExtensionElements(extensionElementMap);
    
    assertEquals(1, serviceTask.getBoundaryEvents().size());
    BoundaryEvent boundaryEvent = serviceTask.getBoundaryEvents().get(0);
    assertEquals("timerEvent", boundaryEvent.getId());
    assertEquals(1, boundaryEvent.getEventDefinitions().size());
    assertTrue(boundaryEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition);
    extensionElementMap = boundaryEvent.getEventDefinitions().get(0).getExtensionElements();
    validateExtensionElements(extensionElementMap);
  }
  
  protected void validateExecutionListeners(List<ActivitiListener> listeners) {
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
  }
  
  protected void validateExtensionElements(Map<String, List<ExtensionElement>> extensionElementMap ) {
    assertEquals(1, extensionElementMap.size());
    
    List<ExtensionElement> extensionElements = extensionElementMap.get("test");
    assertEquals(2, extensionElements.size());
    
    ExtensionElement extensionElement = extensionElements.get(0);
    assertNotNull(extensionElement);
    assertEquals("test", extensionElement.getName());
    assertEquals("custom", extensionElement.getNamespacePrefix());
    assertEquals("http://custom.org/bpmn", extensionElement.getNamespace());
    assertEquals(2, extensionElement.getAttributes().size());

    List<ExtensionAttribute> attributes = extensionElement.getAttributes().get("id");
    assertEquals(1, attributes.size());
    ExtensionAttribute attribute = attributes.get(0);
    assertNotNull(attribute);
    assertEquals("id", attribute.getName());
    assertEquals("test", attribute.getValue());
    assertNull(attribute.getNamespace());
    assertNull(attribute.getNamespacePrefix());
    
    attributes = extensionElement.getAttributes().get("name");
    assertEquals(1, attributes.size());
    attribute = attributes.get(0);
    assertNotNull(attribute);
    assertEquals("name", attribute.getName());
    assertEquals("test", attribute.getValue());
    
    assertEquals(2, extensionElement.getChildElements().size());
    List<ExtensionElement> childExtensions = extensionElement.getChildElements().get("name");
    assertEquals(2, childExtensions.size());
    
    ExtensionElement childExtension = childExtensions.get(0);
    assertNotNull(childExtension);
    assertEquals("name", childExtension.getName());
    assertEquals("custom", childExtension.getNamespacePrefix());
    assertEquals("http://custom.org/bpmn", childExtension.getNamespace());
    assertEquals(0, childExtension.getAttributes().size());
    assertEquals(1, childExtension.getChildElements().size());
    
    List<ExtensionElement> subChildExtensions = childExtension.getChildElements().get("test");
    assertEquals(1, subChildExtensions.size());
    
    childExtension = subChildExtensions.get(0);
    assertNotNull(childExtension);
    assertEquals("test", childExtension.getName());
    assertEquals("custom", childExtension.getNamespacePrefix());
    assertEquals("http://custom.org/bpmn", childExtension.getNamespace());
    assertEquals(0, childExtension.getAttributes().size());
    assertEquals(0, childExtension.getChildElements().size());
    assertEquals("test", childExtension.getElementText());
    
    childExtensions = extensionElement.getChildElements().get("description");
    assertEquals(1, childExtensions.size());
    childExtension = childExtensions.get(0);
    assertNotNull(childExtension);
    assertEquals("description", childExtension.getName());
    assertEquals(1, childExtension.getAttributes().size());
    attributes = childExtension.getAttributes().get("id");
    attribute = attributes.get(0);
    assertNotNull(attribute);
    assertEquals("id", attribute.getName());
    assertEquals("test", attribute.getValue());
    assertEquals("custom2", attribute.getNamespacePrefix());
    assertEquals("http://custom2.org/bpmn", attribute.getNamespace());
    
    extensionElement = extensionElements.get(1);
    assertNotNull(extensionElement);
    assertEquals("test", extensionElement.getName());
    assertEquals("custom", extensionElement.getNamespacePrefix());
    assertEquals("http://custom.org/bpmn", extensionElement.getNamespace());
    assertEquals(2, extensionElement.getAttributes().size());
    
    attributes = extensionElement.getAttributes().get("id");
    assertEquals(1, attributes.size());
    attribute = attributes.get(0);
    assertNotNull(attribute);
    assertEquals("id", attribute.getName());
    assertEquals("test2", attribute.getValue());
    assertNull(attribute.getNamespace());
    assertNull(attribute.getNamespacePrefix());
    
    attributes = extensionElement.getAttributes().get("name");
    assertEquals(1, attributes.size());
    attribute = attributes.get(0);
    assertNotNull(attribute);
    assertEquals("name", attribute.getName());
    assertEquals("test2", attribute.getValue());
  }
}
