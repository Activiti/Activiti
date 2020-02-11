package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class SimpleUserTaskConverterTest extends AbstractConverterTest {

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
        return "simpleusertaskmodel.bpmn";
    }

    private void validateModel(BpmnModel model) throws Exception {
        FlowElement flowElement = model.getMainProcess().getFlowElement("UserTask_0ej3luy");
        assertNotNull(flowElement);
        assertTrue(flowElement instanceof UserTask);

        checkXml(model);
    }

    private void checkXml(BpmnModel model) throws Exception {

        String xml = new String(new BpmnXMLConverter().convertToXML(model),
                                "UTF-8");

        assertThat(xml).contains("incoming>SequenceFlow_12e82d4<",
                                 "outgoing>SequenceFlow_0zx88mt<");

    }
}
