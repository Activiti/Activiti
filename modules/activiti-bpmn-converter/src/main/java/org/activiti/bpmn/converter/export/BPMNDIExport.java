package org.activiti.bpmn.converter.export;

import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.GraphicInfo;

public class BPMNDIExport implements BpmnXMLConstants {

  public static void writeBPMNDI(BpmnModel model, XMLStreamWriter xtw) throws Exception {
    // BPMN DI information
    xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_DIAGRAM, BPMNDI_NAMESPACE);
    
    String processId = null;
    if(model.getPools().size() > 0) {
      processId = "Collaboration";
    } else {
      processId = model.getMainProcess().getId();
    }
    
    xtw.writeAttribute(ATTRIBUTE_ID, "BPMNDiagram_" + processId);

    xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_PLANE, BPMNDI_NAMESPACE);
    xtw.writeAttribute(ATTRIBUTE_DI_BPMNELEMENT, processId);
    xtw.writeAttribute(ATTRIBUTE_ID, "BPMNPlane_" + processId);
    
    for (String elementId : model.getLocationMap().keySet()) {
      xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_SHAPE, BPMNDI_NAMESPACE);
      xtw.writeAttribute(ATTRIBUTE_DI_BPMNELEMENT, elementId);
      xtw.writeAttribute(ATTRIBUTE_ID, "BPMNShape_" + elementId);
      
      GraphicInfo graphicInfo = model.getGraphicInfo(elementId);
      xtw.writeStartElement(OMGDC_PREFIX, ELEMENT_DI_BOUNDS, OMGDC_NAMESPACE);
      xtw.writeAttribute(ATTRIBUTE_DI_HEIGHT, "" + graphicInfo.height);
      xtw.writeAttribute(ATTRIBUTE_DI_WIDTH, "" + graphicInfo.width);
      xtw.writeAttribute(ATTRIBUTE_DI_X, "" + graphicInfo.x);
      xtw.writeAttribute(ATTRIBUTE_DI_Y, "" + graphicInfo.y);
      xtw.writeEndElement();
      
      xtw.writeEndElement();
    }
    
    for (String elementId : model.getFlowLocationMap().keySet()) {
      xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_EDGE, BPMNDI_NAMESPACE);
      xtw.writeAttribute(ATTRIBUTE_DI_BPMNELEMENT, elementId);
      xtw.writeAttribute(ATTRIBUTE_ID, "BPMNEdge_" + elementId);
      
      List<GraphicInfo> graphicInfoList = model.getFlowLocationGraphicInfo(elementId);
      for (GraphicInfo graphicInfo : graphicInfoList) {
        xtw.writeStartElement(OMGDI_PREFIX, ELEMENT_DI_WAYPOINT, OMGDI_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_DI_X, "" + graphicInfo.x);
        xtw.writeAttribute(ATTRIBUTE_DI_Y, "" + graphicInfo.y);
        xtw.writeEndElement();
      }
      
      xtw.writeEndElement();
    }
    
    // end BPMN DI elements
    xtw.writeEndElement();
    xtw.writeEndElement();
  }
}
