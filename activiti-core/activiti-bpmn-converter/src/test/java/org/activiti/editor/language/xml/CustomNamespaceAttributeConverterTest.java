package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

public class CustomNamespaceAttributeConverterTest extends AbstractConverterTest {

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
    return "customnamespaceattributemodel.bpmn";
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

    FlowElement flowElement = model.getMainProcess().getFlowElement("usertask");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(UserTask.class);
    assertThat(flowElement.getId()).isEqualTo("usertask");
    UserTask userTask = (UserTask) flowElement;
    assertThat(userTask.getId()).isEqualTo("usertask");
    assertThat(userTask.getName()).isEqualTo("User Task");

    Map<String, List<ExtensionAttribute>> attributesMap = userTask.getAttributes();
    assertThat(attributesMap).isNotNull();
    assertThat(attributesMap).hasSize(2);

    attributes = attributesMap.get("id");
    assertThat(attributes).isNotNull();
    assertThat(attributes).hasSize(1);
    ExtensionAttribute a = attributes.get(0);
    assertThat(a).isNotNull();
    assertThat(a.getName()).isEqualTo("id");
    assertThat(a.getValue()).isEqualTo("test");
    assertThat(a.getNamespacePrefix()).isEqualTo("custom2");
    assertThat(a.getNamespace()).isEqualTo("http://custom2.org/bpmn");

    attributes = attributesMap.get("attr");
    assertThat(attributes).isNotNull();
    assertThat(attributes).hasSize(1);
    a = attributes.get(0);
    assertThat(a).isNotNull();
    assertThat(a.getName()).isEqualTo("attr");
    assertThat(a.getValue()).isEqualTo("attrValue");
    assertThat(a.getNamespacePrefix()).isEqualTo("custom2");
    assertThat(a.getNamespace()).isEqualTo("http://custom2.org/bpmn");
  }
}
