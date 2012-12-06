package org.activiti.bpmn.converter.export;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;

public class DefinitionsRootExport implements BpmnXMLConstants {

  public static void writeRootElement(XMLStreamWriter xtw) throws Exception {
    xtw.writeStartDocument("UTF-8", "1.0");

    // start definitions root element
    xtw.writeStartElement(ELEMENT_DEFINITIONS);
    xtw.setDefaultNamespace(BPMN2_NAMESPACE);
    xtw.writeDefaultNamespace(BPMN2_NAMESPACE);
    xtw.writeNamespace(XSI_PREFIX, XSI_NAMESPACE);
    xtw.writeNamespace(ACTIVITI_EXTENSIONS_PREFIX, ACTIVITI_EXTENSIONS_NAMESPACE);
    xtw.writeNamespace(BPMNDI_PREFIX, BPMNDI_NAMESPACE);
    xtw.writeNamespace(OMGDC_PREFIX, OMGDC_NAMESPACE);
    xtw.writeNamespace(OMGDI_PREFIX, OMGDI_NAMESPACE);
    xtw.writeAttribute(TYPE_LANGUAGE_ATTRIBUTE, SCHEMA_NAMESPACE);
    xtw.writeAttribute(EXPRESSION_LANGUAGE_ATTRIBUTE, XPATH_NAMESPACE);
    xtw.writeAttribute(TARGET_NAMESPACE_ATTRIBUTE, PROCESS_NAMESPACE);
  }
}
