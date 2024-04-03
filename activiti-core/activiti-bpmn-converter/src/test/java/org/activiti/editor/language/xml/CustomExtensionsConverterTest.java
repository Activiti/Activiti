/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    var bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    var bpmnModel = readXMLFile();
    var parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
    deployProcess(parsedModel);
  }

  protected String getResource() {
    return "customextensionsmodel.bpmn";
  }

  private void validateModel(BpmnModel model) {
    var process = model.getMainProcess();
    assertThat(process.getAttributes()).isNotNull();
    assertThat(process.getAttributes()).hasSize(1);

    var attributes = process.getAttributes().get("version");
    assertThat(attributes).isNotNull();
    assertThat(attributes).hasSize(1);

    var attribute = attributes.getFirst();
    // custom:version = "9"
    assertThat(attribute).isNotNull();
    assertThat(attribute.getNamespace()).isEqualTo("http://custom.org/bpmn");
    assertThat(attribute.getNamespacePrefix()).isEqualTo("custom");
    assertThat(attribute.getName()).isEqualTo("version");
    assertThat(attribute.getValue()).isEqualTo("9");

    var listeners = model.getMainProcess().getExecutionListeners();
    validateExecutionListeners(listeners);
    var extensionElementMap = model.getMainProcess()
      .getExtensionElements();
    validateExtensionElements(extensionElementMap);

    var flowElement = model.getMainProcess().getFlowElement("servicetask");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(ServiceTask.class);
    assertThat(flowElement.getId()).isEqualTo("servicetask");

    var serviceTask = (ServiceTask) flowElement;
    assertThat(serviceTask.getId()).isEqualTo("servicetask");
    assertThat(serviceTask.getName()).isEqualTo("Service task");

    var fields = serviceTask.getFieldExtensions();
    assertThat(fields).hasSize(2);
    FieldExtension field = fields.getFirst();
    assertThat(field.getFieldName()).isEqualTo("testField");
    assertThat(field.getStringValue()).isEqualTo("test");
    field = fields.get(1);
    assertThat(field.getFieldName()).isEqualTo("testField2");
    assertThat(field.getExpression()).isEqualTo("${test}");

    listeners = serviceTask.getExecutionListeners();
    validateExecutionListeners(listeners);

    extensionElementMap = serviceTask.getExtensionElements();
    validateExtensionElements(extensionElementMap);

    assertThat(serviceTask.getBoundaryEvents()).hasSize(1);
    BoundaryEvent boundaryEvent = serviceTask.getBoundaryEvents().getFirst();
    assertThat(boundaryEvent.getId()).isEqualTo("timerEvent");
    assertThat(boundaryEvent.getEventDefinitions()).hasSize(1);
    assertThat(boundaryEvent.getEventDefinitions().getFirst()).isInstanceOf(TimerEventDefinition.class);
    extensionElementMap = boundaryEvent.getEventDefinitions().getFirst().getExtensionElements();
    validateExtensionElements(extensionElementMap);
  }

  protected void validateExecutionListeners(List<ActivitiListener> listeners) {
    assertThat(listeners).hasSize(3);
    ActivitiListener listener = listeners.getFirst();
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(
      listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("org.test.TestClass");
    assertThat(listener.getEvent()).isEqualTo("start");
    assertThat(listener.getOnTransaction()).isEqualTo("before-commit");
    assertThat(listener.getCustomPropertiesResolverImplementation()).isEqualTo(
      "org.test.TestResolverClass");
    listener = listeners.get(1);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(
      listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("${testExpression}");
    assertThat(listener.getEvent()).isEqualTo("end");
    assertThat(listener.getOnTransaction()).isEqualTo("committed");
    assertThat(listener.getCustomPropertiesResolverImplementation()).isEqualTo(
      "${testResolverExpression}");
    listener = listeners.get(2);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(
      listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("${delegateExpression}");
    assertThat(listener.getEvent()).isEqualTo("start");
    assertThat(listener.getOnTransaction()).isEqualTo("rolled-back");
    assertThat(listener.getCustomPropertiesResolverImplementation()).isEqualTo(
      "${delegateResolverExpression}");

  }

  protected void validateExtensionElements(
    Map<String, List<ExtensionElement>> extensionElementMap) {
    assertThat(extensionElementMap).hasSize(1);

    List<ExtensionElement> extensionElements = extensionElementMap.get("test");
    assertThat(extensionElements).hasSize(2);

    ExtensionElement extensionElement = extensionElements.getFirst();
    assertThat(extensionElement).isNotNull();
    assertThat(extensionElement.getName()).isEqualTo("test");
    assertThat(extensionElement.getNamespacePrefix()).isEqualTo("custom");
    assertThat(extensionElement.getNamespace()).isEqualTo("http://custom.org/bpmn");
    assertThat(extensionElement.getAttributes()).hasSize(2);

    List<ExtensionAttribute> attributes = extensionElement.getAttributes().get("id");
    assertThat(attributes).hasSize(1);
    ExtensionAttribute attribute = attributes.getFirst();
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

    ExtensionElement childExtension = childExtensions.getFirst();
    assertThat(childExtension).isNotNull();
    assertThat(childExtension.getName()).isEqualTo("name");
    assertThat(childExtension.getNamespacePrefix()).isEqualTo("custom");
    assertThat(childExtension.getNamespace()).isEqualTo("http://custom.org/bpmn");
    assertThat(childExtension.getAttributes()).hasSize(0);
    assertThat(childExtension.getChildElements()).hasSize(1);

    List<ExtensionElement> subChildExtensions = childExtension.getChildElements().get("test");
    assertThat(subChildExtensions).hasSize(1);

    childExtension = subChildExtensions.getFirst();
    assertThat(childExtension).isNotNull();
    assertThat(childExtension.getName()).isEqualTo("test");
    assertThat(childExtension.getNamespacePrefix()).isEqualTo("custom");
    assertThat(childExtension.getNamespace()).isEqualTo("http://custom.org/bpmn");
    assertThat(childExtension.getAttributes()).hasSize(0);
    assertThat(childExtension.getChildElements()).hasSize(0);
    assertThat(childExtension.getElementText()).isEqualTo("test");

    childExtensions = extensionElement.getChildElements().get("description");
    assertThat(childExtensions).hasSize(1);
    childExtension = childExtensions.getFirst();
    assertThat(childExtension).isNotNull();
    assertThat(childExtension.getName()).isEqualTo("description");
    assertThat(childExtension.getAttributes()).hasSize(1);
    attributes = childExtension.getAttributes().get("id");
    attribute = attributes.getFirst();
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
    attribute = attributes.getFirst();
    assertThat(attribute).isNotNull();
    assertThat(attribute.getName()).isEqualTo("name");
    assertThat(attribute.getValue()).isEqualTo("test2");
  }
}
