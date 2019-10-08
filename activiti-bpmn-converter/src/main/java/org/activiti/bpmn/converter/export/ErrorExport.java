package org.activiti.bpmn.converter.export;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Error;

public class ErrorExport implements BpmnXMLConstants {

    public static void writeError(BpmnModel model,
                                  XMLStreamWriter xtw) throws Exception {
        for (Error error : model.getErrors().values()) {
            xtw.writeStartElement(ELEMENT_ERROR);
            xtw.writeAttribute(ATTRIBUTE_ID,
                               error.getId());
            xtw.writeAttribute(ATTRIBUTE_NAME,
                               error.getName());
            xtw.writeAttribute(ATTRIBUTE_ERROR_CODE,
                               error.getErrorCode());
            xtw.writeEndElement();
        }
    }
}
