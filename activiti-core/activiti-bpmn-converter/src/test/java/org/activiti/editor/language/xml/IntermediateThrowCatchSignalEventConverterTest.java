package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.junit.jupiter.api.Test;

public class IntermediateThrowCatchSignalEventConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();

    validateModel(exportAndReadXMLFile(bpmnModel));
  }

  @Override
  protected String getResource() {
    return "intermediate-throw-catch-signal-event.bpmn20.xml";
  }

  private void validateModel(BpmnModel model)  throws Exception {
      assertThat(model.getDefinitionsAttributes()).hasSize(2);

      checkThrowEvent(model, "IntermediateThrowEvent_1kdg748", "Signal_1xjaioc");
      checkCatchEvent(model, "IntermediateThrowEvent_1uj8tzz", "Signal_1xjaioc");

      checkXml(model);
  }

  private void checkThrowEvent(BpmnModel model,
                               String id,
                               String signalRef) {

      FlowElement flowElement = model.getMainProcess().getFlowElement(id);
      assertThat(flowElement).isNotNull();
      assertThat(flowElement).isInstanceOf(ThrowEvent.class);

      ThrowEvent throwEvent = (ThrowEvent) flowElement;

      assertThat(throwEvent.getIncomingFlows()).hasSize(1);
      assertThat(throwEvent.getOutgoingFlows()).hasSize(1);
      assertThat(throwEvent.getEventDefinitions()).hasSize(1);

      assertThat(throwEvent.getIncomingFlows().get(0).getXmlRowNumber()).isLessThan(throwEvent.getEventDefinitions().get(0).getXmlRowNumber());

      checkSignalEventDefinition(throwEvent, signalRef);
  }

  private void checkCatchEvent(BpmnModel model,
                               String id,
                               String signalRef) {

      FlowElement flowElement = model.getMainProcess().getFlowElement(id);
      assertThat(flowElement).isNotNull();
      assertThat(flowElement).isInstanceOf(IntermediateCatchEvent.class);

      IntermediateCatchEvent catchEvent = (IntermediateCatchEvent) flowElement;

      assertThat(catchEvent.getIncomingFlows()).hasSize(1);
      assertThat(catchEvent.getOutgoingFlows()).hasSize(1);
      assertThat(catchEvent.getEventDefinitions()).hasSize(1);

      assertThat(catchEvent.getIncomingFlows().get(0).getXmlRowNumber()).isLessThan(catchEvent.getEventDefinitions().get(0).getXmlRowNumber());

      checkSignalEventDefinition(catchEvent, signalRef);
  }

  private void checkSignalEventDefinition(Event event, String signalRef) {

      assertThat(event.getEventDefinitions().get(0)).isInstanceOf(SignalEventDefinition.class);
      SignalEventDefinition signalEventDefinition = (SignalEventDefinition) event.getEventDefinitions().get(0);

      assertThat(signalEventDefinition.getSignalRef()).isEqualTo(signalRef);
  }

  private void checkXml(BpmnModel model) throws Exception {

      String xml = new String(new BpmnXMLConverter().convertToXML(model),
                              "UTF-8");

      assertThat(xml).contains("incoming>SequenceFlow_0wsx2cf<",
                               "outgoing>SequenceFlow_1fmvq3w<",
                               "incoming>SequenceFlow_1fmvq3w<",
                               "outgoing>SequenceFlow_18ev42o<");

  }
}
