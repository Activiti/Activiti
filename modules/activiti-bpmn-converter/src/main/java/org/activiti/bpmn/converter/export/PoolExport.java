package org.activiti.bpmn.converter.export;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Pool;
import org.apache.commons.lang.StringUtils;

public class PoolExport implements BpmnXMLConstants {

  public static void writePools(BpmnModel model, XMLStreamWriter xtw) throws Exception {
    if(model.getPools().size() > 0) {
      xtw.writeStartElement(ELEMENT_COLLABORATION);
      xtw.writeAttribute(ATTRIBUTE_ID, "Collaboration");
      for (Pool pool : model.getPools()) {
        xtw.writeStartElement(ELEMENT_PARTICIPANT);
        xtw.writeAttribute(ATTRIBUTE_ID, pool.getId());
        if(StringUtils.isNotEmpty(pool.getName())) {
          xtw.writeAttribute(ATTRIBUTE_NAME, pool.getName());
        }
        xtw.writeAttribute(ATTRIBUTE_PROCESS_REF, pool.getProcessRef());
        xtw.writeEndElement();
      }
      xtw.writeEndElement();
    }
  }
}
