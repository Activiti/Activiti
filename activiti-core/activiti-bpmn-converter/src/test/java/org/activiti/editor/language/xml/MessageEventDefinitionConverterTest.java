package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.ThrowEvent;
import org.junit.jupiter.api.Test;

public class MessageEventDefinitionConverterTest extends AbstractConverterTest {

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
        Message message = model.getMessage("Message_1");

        assertThat(message).isNotNull()
                           .extracting(Message::getId,
                                       Message::getName)
                           .contains("Message_1",
                                     "catchMessage");

        assertThat(model.getProcessById("intermediateCatchProcess")
                        .getFlowElements()).filteredOn(IntermediateCatchEvent.class::isInstance)
                                           .flatExtracting("eventDefinitions")
                                           .extracting("messageRef", "correlationKey")
                                           .contains(tuple("Message_1", "${correlationId}"));

        assertThat(model.getProcessById("intermediateThrowProcess")
                        .getFlowElements()).filteredOn(ThrowEvent.class::isInstance)
                                           .flatExtracting("eventDefinitions")
                                           .extracting("messageRef", "correlationKey")
                                           .contains(tuple("Message_1", "${correlationId}"));

        assertThat(model.getProcessById("endThrowProcess")
                        .getFlowElements()).filteredOn(EndEvent.class::isInstance)
                                           .flatExtracting("eventDefinitions")
                                           .extracting("messageRef", "correlationKey")
                                           .contains(tuple("Message_1", "${correlationId}"));

        assertThat(model.getProcessById("boundaryCatchProcess")
                        .getFlowElements()).filteredOn(BoundaryEvent.class::isInstance)
                                           .flatExtracting("eventDefinitions")
                                           .extracting("messageRef", "correlationKey")
                                           .contains(tuple("Message_1", "${correlationId}"));

        assertThat(model.getProcessById("startMessageEventSubprocess")
                        .getFlowElements()).filteredOn(EventSubProcess.class::isInstance)
                                           .flatExtracting("flowElements")
                                           .filteredOn(StartEvent.class::isInstance)
                                           .flatExtracting("eventDefinitions")
                                           .extracting("messageRef", "correlationKey")
                                           .contains(tuple("Message_1", "${correlationId}"));

        assertThat(model.getProcessById("startMessageProcess")
                        .getFlowElements()).filteredOn(StartEvent.class::isInstance)
                                           .flatExtracting("eventDefinitions")
                                           .extracting("messageRef", "correlationKey")
                                           .contains(tuple("Message_1", null));

        assertThat(model.getProcessById("boundaryCatchSubrocess")
                        .getFlowElements()).filteredOn(BoundaryEvent.class::isInstance)
                                           .flatExtracting("eventDefinitions")
                                           .extracting("messageRef", "correlationKey")
                                           .contains(tuple("Message_1", "${correlationId}"));

        assertThat(model.getProcessById("intermediateCatchMessageExpressionProcess")
                        .getFlowElements()).filteredOn(IntermediateCatchEvent.class::isInstance)
                                           .flatExtracting("eventDefinitions")
                                           .extracting("messageRef", "messageExpression", "correlationKey")
                                           .contains(tuple(null, "catchMessage", "${correlationId}"));

    }

    protected String getResource() {
        return "MessageEventDefinitionConverterTest.bpmn";
    }
}
