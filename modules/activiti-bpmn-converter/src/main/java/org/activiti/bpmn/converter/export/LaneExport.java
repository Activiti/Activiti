package org.activiti.bpmn.converter.export;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang.StringUtils;

public class LaneExport implements BpmnXMLConstants {

  public static void writeLanes(Process process, XMLStreamWriter xtw) throws Exception {
    if(process.getLanes().size() > 0) {
      xtw.writeStartElement(ELEMENT_LANESET);
      xtw.writeAttribute(ATTRIBUTE_ID, "laneSet_" + process.getId());
      for (Lane lane : process.getLanes()) {
        xtw.writeStartElement(ELEMENT_LANE);
        xtw.writeAttribute(ATTRIBUTE_ID, lane.getId());
        if(StringUtils.isNotEmpty(lane.getName())) {
          xtw.writeAttribute(ATTRIBUTE_NAME, lane.getName());
        }
        
        for (String flowNodeRef : lane.getFlowReferences()) {
          xtw.writeStartElement(ATTRIBUTE_FLOWNODE_REF);
          xtw.writeCharacters(flowNodeRef);
          xtw.writeEndElement();
        }
        
        xtw.writeEndElement();
      }
      xtw.writeEndElement();
    }
  }
}
