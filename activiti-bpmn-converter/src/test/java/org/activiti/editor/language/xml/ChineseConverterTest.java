package org.activiti.editor.language.xml;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class ChineseConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    assertNotNull("BPMN Model XML not found", bpmnModel);
    deployProcess(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    assertNotNull("BPMN Model XML not found", bpmnModel);
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    deployProcess(parsedModel);
  }

  protected String getResource() {
    return "chinese.bpmn";
  }
}
