package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Signal;
import org.junit.Test;

public class SignalConverterTest extends AbstractConverterTest {

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
  }

  private void validateModel(BpmnModel model) {
    Collection<Signal> signals = model.getSignals();
    assertEquals(2, signals.size());
  }

  protected String getResource() {
    return "signaltest.bpmn";
  }
}