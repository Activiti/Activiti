package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Task;
import org.junit.Test;

public class TaskConverterTest extends AbstractConverterTest {

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
        deployProcess(parsedModel);
    }

    @Override
    protected String getResource() {
        return "taskmodel.bpmn";
    }

    private void validateModel(BpmnModel model) throws Exception {
        FlowElement flowElement = model.getMainProcess().getFlowElement("Task_17unmd8");
        assertNotNull(flowElement);
        assertTrue(flowElement instanceof Task);

        checkXml(model);
    }

    private void checkXml(BpmnModel model) throws Exception {

        String xml = new String(new BpmnXMLConverter().convertToXML(model),
                                "UTF-8");

        assertThat(xml).contains("incoming>SequenceFlow_12r6kuh<",
                                 "outgoing>SequenceFlow_1h7t0iw<");

    }
}
