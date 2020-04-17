package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

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
    }

    @Override
    protected String getResource() {
        return "simpleUserTaskModel.bpmn";
    }

    private void validateModel(BpmnModel model) throws Exception {
        FlowElement flowElement = model.getMainProcess().getFlowElement("UserTask_0ej3luy");
        assertThat(flowElement).isNotNull();
        assertThat(flowElement).isInstanceOf(UserTask.class);

        checkXml(model);
    }

    private void checkXml(BpmnModel model) throws Exception {

        String xml = new String(new BpmnXMLConverter().convertToXML(model),
                                "UTF-8");

        assertThat(xml).contains("incoming>SequenceFlow_12e82d4<",
                                 "outgoing>SequenceFlow_0zx88mt<");

    }
}
