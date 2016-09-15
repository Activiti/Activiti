package org.activiti.editor.language.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.junit.Test;

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
    assertEquals("PT5M", timerEvent.getTimeDuration());

    StartEvent start = (StartEvent) model.getMainProcess().getFlowElement("theStart");
    assertNotNull(start);
    TimerEventDefinition startTimerEvent = (TimerEventDefinition) start.getEventDefinitions().get(0);
    assertThat(startTimerEvent.getCalendarName(), is("custom"));
    assertEquals("R2/PT5S", startTimerEvent.getTimeCycle());
    assertEquals("${EndDate}", startTimerEvent.getEndDate());

    BoundaryEvent boundaryTimer = (BoundaryEvent) model.getMainProcess().getFlowElement("boundaryTimer");
    assertNotNull(boundaryTimer);
    TimerEventDefinition boundaryTimerEvent = (TimerEventDefinition) boundaryTimer.getEventDefinitions().get(0);
    assertThat(boundaryTimerEvent.getCalendarName(), is("custom"));
    assertEquals("PT10S", boundaryTimerEvent.getTimeDuration());
  }
}
