package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.jupiter.api.Test;

public class OtherToolImportConverterTest extends AbstractConverterTest {

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
    return "othertoolimport.bpmn";
  }

  private void validateModel(BpmnModel model) {
    org.activiti.bpmn.model.Process process = model.getProcess("_GQ4P0PUQEeK4teimjV5_yg");
    assertThat(process).isNotNull();
    assertThat(process.getId()).isEqualTo("Carpet_Plus");
    assertThat(process.getName()).isEqualTo("Carpet-Plus");
    assertThat(process.isExecutable()).isTrue();
  }
}
