package org.activiti.editor.language.xml;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.jupiter.api.Test;

public class ChineseConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    deployProcess(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    deployProcess(parsedModel);
  }

  protected String getResource() {
    return "chinese.bpmn";
  }
}
