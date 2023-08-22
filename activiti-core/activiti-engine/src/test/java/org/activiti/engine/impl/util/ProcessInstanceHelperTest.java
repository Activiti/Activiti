package org.activiti.engine.impl.util;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.assertj.core.api.Assertions.assertThat;


public class ProcessInstanceHelperTest {

    private ProcessInstanceHelper processInstancehelper;

    private MockedStatic mockedStatic;

    private BpmnXMLConverter converter;


    @Before
    public void setup() throws XMLStreamException {
        converter = new BpmnXMLConverter();
        mockedStatic = mockStatic(ProcessDefinitionUtil.class);
        processInstancehelper = new ProcessInstanceHelper();
    }

    @After
    public void clear() {
        mockedStatic.close();
    }

    @Test
    public void testInitialFlowByMessageForEvent1() throws XMLStreamException {
        String MESSAGE_NAME = "event1";
        String allMessagesExistPath = "/org/activiti/engine/test/impl/util/StartProcessInstanceByMessage.testInitialFlowElementByMessage.bpmn20.xml";
        InputStream fin = this.getClass().getResourceAsStream(allMessagesExistPath);
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(fin);
        BpmnModel bpmnModel = converter.convertToBpmnModel(reader);
        Process process = bpmnModel.getProcessById("Process_1");
        mockedStatic.when(() -> ProcessDefinitionUtil.getBpmnModel(any())).thenReturn(bpmnModel);
        FlowElement initialFlowElementByMessage = processInstancehelper.getInitialFlowElementByMessage(process, new ProcessDefinitionEntityImpl(), MESSAGE_NAME);
        assertThat(initialFlowElementByMessage).isNotNull();
        assertThat(initialFlowElementByMessage.getName()).isEqualTo("start1");
        reader.close();
    }

    @Test
    public void testInitialFlowByMessageForEvent2() throws XMLStreamException {
        String MESSAGE_NAME = "event2";
        String allMessagesExistPath = "/org/activiti/engine/test/impl/util/StartProcessInstanceByMessage.testInitialFlowElementByMessage.bpmn20.xml";
        InputStream fin = this.getClass().getResourceAsStream(allMessagesExistPath);
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(fin);
        BpmnModel bpmnModel = converter.convertToBpmnModel(reader);
        Process process = bpmnModel.getProcessById("Process_1");
        mockedStatic.when(() -> ProcessDefinitionUtil.getBpmnModel(any())).thenReturn(bpmnModel);
        FlowElement initialFlowElementByMessage = processInstancehelper.getInitialFlowElementByMessage(process, new ProcessDefinitionEntityImpl(), MESSAGE_NAME);
        assertThat(initialFlowElementByMessage).isNotNull();
        assertThat(initialFlowElementByMessage.getName()).isEqualTo("start2");
        System.out.println(initialFlowElementByMessage.getName());
    }

    // When messageName passed does not match any messageRef
    @Test
    public void testInitialFlowByMessageForMessageNameNotPresentInMessageRefs() throws XMLStreamException {
        String allMessagesExistPath = "/org/activiti/engine/test/impl/util/StartProcessInstanceByMessage.testInitialFlowElementByMessage.bpmn20.xml";
        String MESSAGE_NAME = "eventinvalid";
        InputStream fin = this.getClass().getResourceAsStream(allMessagesExistPath);
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(fin);
        BpmnModel bpmnModel = converter.convertToBpmnModel(reader);
        Process process = bpmnModel.getProcessById("Process_1");
        mockedStatic.when(() -> ProcessDefinitionUtil.getBpmnModel(any())).thenReturn(bpmnModel);
        FlowElement initialFlowElementByMessage = processInstancehelper.getInitialFlowElementByMessage(process, new ProcessDefinitionEntityImpl(), MESSAGE_NAME);
        assertThat(initialFlowElementByMessage).isNotNull();
        assertThat(initialFlowElementByMessage.getName()).isEqualTo("start1");
    }

    @Test
    public void testInitialFlowByMessageForNoMessageExists() throws XMLStreamException {
        String noMessagesExistPath = "/org/activiti/engine/test/impl/util/StartProcessInstanceByMessage.testNoMessagePresent.bpmn20.xml";
        String MESSAGE_NAME = "eventinvalid";
        InputStream fin = this.getClass().getResourceAsStream(noMessagesExistPath);
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(fin);
        BpmnModel bpmnModel = converter.convertToBpmnModel(reader);
        Process process = bpmnModel.getProcessById("Process_No_Message");
        mockedStatic.when(() -> ProcessDefinitionUtil.getBpmnModel(any())).thenReturn(bpmnModel);
        FlowElement initialFlowElementByMessage = processInstancehelper.getInitialFlowElementByMessage(process, new ProcessDefinitionEntityImpl(), MESSAGE_NAME);
        assertThat(initialFlowElementByMessage).isNull();
    }


    @Test
    public void testInitialFlowByMessageForOnlyOneMessageExist() throws XMLStreamException {
        String noMessagesExistPath = "/org/activiti/engine/test/impl/util/StartProcessInstanceByMessage.testOnlyOneMessageExist.bpmn20.xml";
        String MESSAGE_NAME = "eventinvalid";
        InputStream fin = this.getClass().getResourceAsStream(noMessagesExistPath);
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(fin);
        BpmnModel bpmnModel = converter.convertToBpmnModel(reader);
        Process process = bpmnModel.getProcessById("Process_One_Message");
        mockedStatic.when(() -> ProcessDefinitionUtil.getBpmnModel(any())).thenReturn(bpmnModel);
        FlowElement initialFlowElementByMessage = processInstancehelper.getInitialFlowElementByMessage(process, new ProcessDefinitionEntityImpl(), MESSAGE_NAME);
        assertThat(initialFlowElementByMessage).isNotNull();
        assertThat(initialFlowElementByMessage.getName()).isEqualTo("start2");
    }


}
