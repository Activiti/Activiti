package org.activiti.editor.language.xml;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.Test;

public class ChineseConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
  }

  protected String getResource() {
    return "chinese.bpmn";
  }
}
