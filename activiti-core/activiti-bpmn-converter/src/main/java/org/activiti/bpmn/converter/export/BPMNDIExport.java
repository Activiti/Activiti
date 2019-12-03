/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.bpmn.converter.export;

import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.MessageFlow;
import org.activiti.bpmn.model.SubProcess;
import org.apache.commons.lang3.StringUtils;

public class BPMNDIExport implements BpmnXMLConstants {

  public static void writeBPMNDI(BpmnModel model, XMLStreamWriter xtw) throws Exception {
    // BPMN DI information
    xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_DIAGRAM, BPMNDI_NAMESPACE);

    String processId = null;
    if (!model.getPools().isEmpty()) {
      processId = "Collaboration";
    } else {
      processId = model.getMainProcess().getId();
    }

    xtw.writeAttribute(ATTRIBUTE_ID, "BPMNDiagram_" + processId);

    xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_PLANE, BPMNDI_NAMESPACE);
    xtw.writeAttribute(ATTRIBUTE_DI_BPMNELEMENT, processId);
    xtw.writeAttribute(ATTRIBUTE_ID, "BPMNPlane_" + processId);

    for (String elementId : model.getLocationMap().keySet()) {

      if (model.getFlowElement(elementId) != null || model.getArtifact(elementId) != null || model.getPool(elementId) != null || model.getLane(elementId) != null) {

        xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_SHAPE, BPMNDI_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_DI_BPMNELEMENT, elementId);
        xtw.writeAttribute(ATTRIBUTE_ID, "BPMNShape_" + elementId);

        GraphicInfo graphicInfo = model.getGraphicInfo(elementId);
        FlowElement flowElement = model.getFlowElement(elementId);
        if (flowElement instanceof SubProcess && graphicInfo.getExpanded() != null) {
          xtw.writeAttribute(ATTRIBUTE_DI_IS_EXPANDED, String.valueOf(graphicInfo.getExpanded()));
        }

        xtw.writeStartElement(OMGDC_PREFIX, ELEMENT_DI_BOUNDS, OMGDC_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_DI_HEIGHT, "" + graphicInfo.getHeight());
        xtw.writeAttribute(ATTRIBUTE_DI_WIDTH, "" + graphicInfo.getWidth());
        xtw.writeAttribute(ATTRIBUTE_DI_X, "" + graphicInfo.getX());
        xtw.writeAttribute(ATTRIBUTE_DI_Y, "" + graphicInfo.getY());
        xtw.writeEndElement();

        xtw.writeEndElement();
      }
    }

    for (String elementId : model.getFlowLocationMap().keySet()) {

      if (model.getFlowElement(elementId) != null || model.getArtifact(elementId) != null || model.getMessageFlow(elementId) != null) {

        xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_EDGE, BPMNDI_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_DI_BPMNELEMENT, elementId);
        xtw.writeAttribute(ATTRIBUTE_ID, "BPMNEdge_" + elementId);

        List<GraphicInfo> graphicInfoList = model.getFlowLocationGraphicInfo(elementId);
        for (GraphicInfo graphicInfo : graphicInfoList) {
          xtw.writeStartElement(OMGDI_PREFIX, ELEMENT_DI_WAYPOINT, OMGDI_NAMESPACE);
          xtw.writeAttribute(ATTRIBUTE_DI_X, "" + graphicInfo.getX());
          xtw.writeAttribute(ATTRIBUTE_DI_Y, "" + graphicInfo.getY());
          xtw.writeEndElement();
        }

        GraphicInfo labelGraphicInfo = model.getLabelGraphicInfo(elementId);
        FlowElement flowElement = model.getFlowElement(elementId);
        MessageFlow messageFlow = null;
        if (flowElement == null) {
          messageFlow = model.getMessageFlow(elementId);
        }

        boolean hasName = false;
        if (flowElement != null && StringUtils.isNotEmpty(flowElement.getName())) {
          hasName = true;

        } else if (messageFlow != null && StringUtils.isNotEmpty(messageFlow.getName())) {
          hasName = true;
        }

        if (labelGraphicInfo != null && hasName) {
          xtw.writeStartElement(BPMNDI_PREFIX, ELEMENT_DI_LABEL, BPMNDI_NAMESPACE);
          xtw.writeStartElement(OMGDC_PREFIX, ELEMENT_DI_BOUNDS, OMGDC_NAMESPACE);
          xtw.writeAttribute(ATTRIBUTE_DI_HEIGHT, "" + labelGraphicInfo.getHeight());
          xtw.writeAttribute(ATTRIBUTE_DI_WIDTH, "" + labelGraphicInfo.getWidth());
          xtw.writeAttribute(ATTRIBUTE_DI_X, "" + labelGraphicInfo.getX());
          xtw.writeAttribute(ATTRIBUTE_DI_Y, "" + labelGraphicInfo.getY());
          xtw.writeEndElement();
          xtw.writeEndElement();
        }

        xtw.writeEndElement();
      }
    }

    // end BPMN DI elements
    xtw.writeEndElement();
    xtw.writeEndElement();
  }
}
