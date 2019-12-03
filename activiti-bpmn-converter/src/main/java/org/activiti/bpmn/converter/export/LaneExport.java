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

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang3.StringUtils;

public class LaneExport implements BpmnXMLConstants {

  public static void writeLanes(Process process, XMLStreamWriter xtw) throws Exception {
    if (!process.getLanes().isEmpty()) {
      xtw.writeStartElement(BPMN2_PREFIX, ELEMENT_LANESET, BPMN2_NAMESPACE);
      xtw.writeAttribute(ATTRIBUTE_ID, "laneSet_" + process.getId());
      for (Lane lane : process.getLanes()) {
        xtw.writeStartElement(BPMN2_PREFIX, ELEMENT_LANE, BPMN2_NAMESPACE);
        xtw.writeAttribute(ATTRIBUTE_ID, lane.getId());

        if (StringUtils.isNotEmpty(lane.getName())) {
          xtw.writeAttribute(ATTRIBUTE_NAME, lane.getName());
        }

        boolean didWriteExtensionStartElement = BpmnXMLUtil.writeExtensionElements(lane, false, xtw);
        if (didWriteExtensionStartElement) {
          xtw.writeEndElement();
        }

        for (String flowNodeRef : lane.getFlowReferences()) {
          xtw.writeStartElement(BPMN2_PREFIX, ELEMENT_FLOWNODE_REF, BPMN2_NAMESPACE);
          xtw.writeCharacters(flowNodeRef);
          xtw.writeEndElement();
        }

        xtw.writeEndElement();
      }
      xtw.writeEndElement();
    }
  }
}
