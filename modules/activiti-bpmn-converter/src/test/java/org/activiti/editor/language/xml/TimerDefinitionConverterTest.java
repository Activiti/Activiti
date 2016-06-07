package org.activiti.editor.language.xml;

import org.activiti.bpmn.model.*;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class TimerDefinitionConverterTest extends AbstractConverterTest {
  
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
    return "timerCalendarDefinition.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    IntermediateCatchEvent timer = (IntermediateCatchEvent) model.getMainProcess().getFlowElement("timer");
    assertNotNull(timer);
    TimerEventDefinition timerEvent = (TimerEventDefinition) timer.getEventDefinitions().get(0);
    assertThat(timerEvent.getCalendarName(), is("custom"));

    StartEvent start = (StartEvent) model.getMainProcess().getFlowElement("theStart");
    assertNotNull(start);
    TimerEventDefinition startTimerEvent = (TimerEventDefinition) timer.getEventDefinitions().get(0);
    assertThat(startTimerEvent.getCalendarName(), is("custom"));

    BoundaryEvent boundaryTimer = (BoundaryEvent) model.getMainProcess().getFlowElement("boundaryTimer");
    assertNotNull(boundaryTimer);
    TimerEventDefinition boundaryTimerEvent = (TimerEventDefinition) timer.getEventDefinitions().get(0);
    assertThat(boundaryTimerEvent.getCalendarName(), is("custom"));
  }
}
