package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.junit.jupiter.api.Test;

public class CatchEventConverterTest extends AbstractConverterTest {

  @Test
  public void convertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  protected String getResource() {
    return "test.catcheventmodel.json";
  }

  private void validateModel(BpmnModel model) {

    FlowElement timerElement = model.getMainProcess().getFlowElement("timer_evt", true);
    EventDefinition timerEvent = extractEventDefinition(timerElement);
    assertThat(timerEvent).isInstanceOf(TimerEventDefinition.class);
    TimerEventDefinition ted = (TimerEventDefinition) timerEvent;
    assertThat(ted.getTimeDuration()).isEqualTo("PT5M");

    FlowElement signalElement = model.getMainProcess().getFlowElement("signal_evt", true);
    EventDefinition signalEvent = extractEventDefinition(signalElement);
    assertThat(signalEvent).isInstanceOf(SignalEventDefinition.class);
    SignalEventDefinition sed = (SignalEventDefinition) signalEvent;
    assertThat(sed.getSignalRef()).isEqualTo("signal_ref");

    FlowElement messageElement = model.getMainProcess().getFlowElement("message_evt", true);
    EventDefinition messageEvent = extractEventDefinition(messageElement);
    assertThat(messageEvent).isInstanceOf(MessageEventDefinition.class);
    MessageEventDefinition med = (MessageEventDefinition) messageEvent;
    assertThat(med.getMessageRef()).isEqualTo("message_ref");

  }

}
