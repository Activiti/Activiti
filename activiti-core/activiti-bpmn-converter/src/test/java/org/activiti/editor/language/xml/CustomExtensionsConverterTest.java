package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.api.Test;

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
    assertThat(process.getAttributes()).isNotNull();
    assertThat(process.getAttributes()).hasSize(1);
    List<ExtensionAttribute> attributes = process.getAttributes().get("version");
    assertThat(attributes).isNotNull();
    assertThat(attributes).hasSize(1);
    ExtensionAttribute attribute = attributes.get(0);
    // custom:version = "9"
    assertThat(attribute).isNotNull();
    assertThat(attribute.getNamespace()).isEqualTo("http://custom.org/bpmn");
    assertThat(attribute.getNamespacePrefix()).isEqualTo("custom");
    assertThat(attribute.getName()).isEqualTo("version");
    assertThat(attribute.getValue()).isEqualTo("9");

    List<ActivitiListener> listeners = model.getMainProcess().getExecutionListeners();
    validateExecutionListeners(listeners);
    Map<String, List<ExtensionElement>> extensionElementMap = model.getMainProcess().getExtensionElements();
    validateExtensionElements(extensionElementMap);

    FlowElement flowElement = model.getMainProcess().getFlowElement("servicetask");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(ServiceTask.class);
    assertThat(flowElement.getId()).isEqualTo("servicetask");
    ServiceTask serviceTask = (ServiceTask) flowElement;
    assertThat(serviceTask.getId()).isEqualTo("servicetask");
    assertThat(serviceTask.getName()).isEqualTo("Service task");

    List<FieldExtension> fields = serviceTask.getFieldExtensions();
    assertThat(fields).hasSize(2);
    FieldExtension field = (FieldExtension) fields.get(0);
    assertThat(field.getFieldName()).isEqualTo("testField");
    assertThat(field.getStringValue()).isEqualTo("test");
    field = (FieldExtension) fields.get(1);
    assertThat(field.getFieldName()).isEqualTo("testField2");
    assertThat(field.getExpression()).isEqualTo("${test}");

    listeners = serviceTask.getExecutionListeners();
    validateExecutionListeners(listeners);

    extensionElementMap = serviceTask.getExtensionElements();
    validateExtensionElements(extensionElementMap);

    assertThat(serviceTask.getBoundaryEvents()).hasSize(1);
    BoundaryEvent boundaryEvent = serviceTask.getBoundaryEvents().get(0);
    assertThat(boundaryEvent.getId()).isEqualTo("timerEvent");
    assertThat(boundaryEvent.getEventDefinitions()).hasSize(1);
    assertThat(boundaryEvent.getEventDefinitions().get(0)).isInstanceOf(TimerEventDefinition.class);
    extensionElementMap = boundaryEvent.getEventDefinitions().get(0).getExtensionElements();
    validateExtensionElements(extensionElementMap);
  }

  protected void validateExecutionListeners(List<ActivitiListener> listeners) {
    assertThat(listeners).hasSize(3);
    ActivitiListener listener = (ActivitiListener) listeners.get(0);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("org.test.TestClass");
    assertThat(listener.getEvent()).isEqualTo("start");
    assertThat(listener.getOnTransaction()).isEqualTo("before-commit");
    assertThat(listener.getCustomPropertiesResolverImplementation()).isEqualTo("org.test.TestResolverClass");
    listener = (ActivitiListener) listeners.get(1);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("${testExpression}");
    assertThat(listener.getEvent()).isEqualTo("end");
    assertThat(listener.getOnTransaction()).isEqualTo("committed");
    assertThat(listener.getCustomPropertiesResolverImplementation()).isEqualTo("${testResolverExpression}");
    listener = (ActivitiListener) listeners.get(2);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("${delegateExpression}");
    assertThat(listener.getEvent()).isEqualTo("start");
    assertThat(listener.getOnTransaction()).isEqualTo("rolled-back");
    assertThat(listener.getCustomPropertiesResolverImplementation()).isEqualTo("${delegateResolverExpression}");

  }

  protected void validateExtensionElements(Map<String, List<ExtensionElement>> extensionElementMap) {
    assertThat(extensionElementMap).hasSize(1);

    List<ExtensionElement> extensionElements = extensionElementMap.get("test");
    assertThat(extensionElements).hasSize(2);

    ExtensionElement extensionElement = extensionElements.get(0);
    assertThat(extensionElement).isNotNull();
    assertThat(extensionElement.getName()).isEqualTo("test");
    assertThat(extensionElement.getNamespacePrefix()).isEqualTo("custom");
    assertThat(extensionElement.getNamespace()).isEqualTo("http://custom.org/bpmn");
    assertThat(extensionElement.getAttributes()).hasSize(2);

    List<ExtensionAttribute> attributes = extensionElement.getAttributes().get("id");
    assertThat(attributes).hasSize(1);
    ExtensionAttribute attribute = attributes.get(0);
    assertThat(attribute).isNotNull();
    assertThat(attribute.getName()).isEqualTo("id");
    assertThat(attribute.getValue()).isEqualTo("test");
    assertThat(attribute.getNamespace()).isNull();
    assertThat(attribute.getNamespacePrefix()).isNull();

    attributes = extensionElement.getAttributes().get("name");
    assertThat(attributes).hasSize(1);
    attribute = attributes.get(0);
    assertThat(attribute).isNotNull();
    assertThat(attribute.getName()).isEqualTo("name");
    assertThat(attribute.getValue()).isEqualTo("test");

    assertThat(extensionElement.getChildElements()).hasSize(2);
    List<ExtensionElement> childExtensions = extensionElement.getChildElements().get("name");
    assertThat(childExtensions).hasSize(2);

    ExtensionElement childExtension = childExtensions.get(0);
    assertThat(childExtension).isNotNull();
    assertThat(childExtension.getName()).isEqualTo("name");
    assertThat(childExtension.getNamespacePrefix()).isEqualTo("custom");
    assertThat(childExtension.getNamespace()).isEqualTo("http://custom.org/bpmn");
    assertThat(childExtension.getAttributes()).hasSize(0);
    assertThat(childExtension.getChildElements()).hasSize(1);

    List<ExtensionElement> subChildExtensions = childExtension.getChildElements().get("test");
    assertThat(subChildExtensions).hasSize(1);

    childExtension = subChildExtensions.get(0);
    assertThat(childExtension).isNotNull();
    assertThat(childExtension.getName()).isEqualTo("test");
    assertThat(childExtension.getNamespacePrefix()).isEqualTo("custom");
    assertThat(childExtension.getNamespace()).isEqualTo("http://custom.org/bpmn");
    assertThat(childExtension.getAttributes()).hasSize(0);
    assertThat(childExtension.getChildElements()).hasSize(0);
    assertThat(childExtension.getElementText()).isEqualTo("test");

    childExtensions = extensionElement.getChildElements().get("description");
    assertThat(childExtensions).hasSize(1);
    childExtension = childExtensions.get(0);
    assertThat(childExtension).isNotNull();
    assertThat(childExtension.getName()).isEqualTo("description");
    assertThat(childExtension.getAttributes()).hasSize(1);
    attributes = childExtension.getAttributes().get("id");
    attribute = attributes.get(0);
    assertThat(attribute).isNotNull();
    assertThat(attribute.getName()).isEqualTo("id");
    assertThat(attribute.getValue()).isEqualTo("test");
    assertThat(attribute.getNamespacePrefix()).isEqualTo("custom2");
    assertThat(attribute.getNamespace()).isEqualTo("http://custom2.org/bpmn");

    extensionElement = extensionElements.get(1);
    assertThat(extensionElement).isNotNull();
    assertThat(extensionElement.getName()).isEqualTo("test");
    assertThat(extensionElement.getNamespacePrefix()).isEqualTo("custom");
    assertThat(extensionElement.getNamespace()).isEqualTo("http://custom.org/bpmn");
    assertThat(extensionElement.getAttributes()).hasSize(2);

    attributes = extensionElement.getAttributes().get("id");
    assertThat(attributes).hasSize(1);
    attribute = attributes.get(0);
    assertThat(attribute).isNotNull();
    assertThat(attribute.getName()).isEqualTo("id");
    assertThat(attribute.getValue()).isEqualTo("test2");
    assertThat(attribute.getNamespace()).isNull();
    assertThat(attribute.getNamespacePrefix()).isNull();

    attributes = extensionElement.getAttributes().get("name");
    assertThat(attributes).hasSize(1);
    attribute = attributes.get(0);
    assertThat(attribute).isNotNull();
    assertThat(attribute.getName()).isEqualTo("name");
    assertThat(attribute.getValue()).isEqualTo("test2");
  }
}
