package org.activiti.editor.language.xml;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Error;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

public class ErrorConverterTest extends AbstractConverterTest {

    @Test
    public void testConversionFromXmlToBPMNModel() throws Exception {
        BpmnModel bpmnModel = readXMLFile();
        assertThat(bpmnModel.getErrors().values())
                .usingFieldByFieldElementComparator()
                .containsOnlyElementsOf(newArrayList(new Error("Error_0v4rsz5",
                                                               "ok",
                                                               "200"),
                                                     new Error("Error_02htlc0",
                                                               "conflict",
                                                               "409")));
    }

    @Test
    public void testConversionFromBPMNModelToXml() throws Exception {
        BpmnModel bpmnModel = readXMLFile();
        byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel);
        String convertedXml = new String(xml,
                                         "UTF-8");
        assertThat(convertedXml).contains("<error id=\"Error_0v4rsz5\" name=\"ok\" errorCode=\"200\">");
        assertThat(convertedXml).contains("<error id=\"Error_02htlc0\" name=\"conflict\" errorCode=\"409\">");
    }

    @Override
    protected String getResource() {
        return "error.bpmn";
    }
}
