package org.activiti.editor.language.xml;

import org.activiti.bpmn.model.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class SubProcessCompensationConverterTest extends AbstractConverterTest {

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
    return "subprocesscompensationmodel.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("start1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof StartEvent);
    assertEquals("start1", flowElement.getId());

    flowElement = model.getMainProcess().getFlowElement("scriptTask1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof ScriptTask);
    assertEquals("scriptTask1", flowElement.getId());
    ScriptTask scriptTask = (ScriptTask) flowElement;
    assertTrue(scriptTask.getScriptFormat().equals("groovy"));

    flowElement = model.getMainProcess().getFlowElement("subprocess1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof SubProcess);
    assertEquals("subprocess1", flowElement.getId());
    SubProcess subProcess = (SubProcess) flowElement;
    assertTrue(subProcess.isForCompensation());

    flowElement = model.getMainProcess().getFlowElement("boundaryEvent1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof BoundaryEvent);
    assertEquals("boundaryEvent1", flowElement.getId());
    BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
    assertNotNull(boundaryEvent.getAttachedToRef());
    assertEquals("scriptTask1", boundaryEvent.getAttachedToRef().getId());
  }
}
