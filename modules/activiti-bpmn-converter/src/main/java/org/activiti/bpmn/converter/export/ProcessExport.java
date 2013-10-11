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
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProcessExport implements BpmnXMLConstants {
  /**
   * default attributes taken from process instance attributes
   */
  protected static final List<ExtensionAttribute> defaultAttributes = Arrays.asList(
          new ExtensionAttribute(ATTRIBUTE_ID)
          ,new ExtensionAttribute(ATTRIBUTE_NAME)
          ,new ExtensionAttribute(ATTRIBUTE_PROCESS_EXECUTABLE)
      );

  public static void writeProcess(Process process, XMLStreamWriter xtw) throws Exception {
    // start process element
    xtw.writeStartElement(ELEMENT_PROCESS);
    xtw.writeAttribute(ATTRIBUTE_ID, process.getId());

    if (StringUtils.isNotEmpty(process.getName())) {
      xtw.writeAttribute(ATTRIBUTE_NAME, process.getName());
    }

    xtw.writeAttribute(ATTRIBUTE_PROCESS_EXECUTABLE, Boolean.toString(process.isExecutable()));

    if (process.getCandidateStarterUsers().size() > 0) {
      xtw.writeAttribute(ACTIVITI_EXTENSIONS_PREFIX, ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_PROCESS_CANDIDATE_USERS,
          BpmnXMLUtil.convertToDelimitedString(process.getCandidateStarterUsers()));
    }

    if (process.getCandidateStarterGroups().size() > 0) {
      xtw.writeAttribute(ACTIVITI_EXTENSIONS_PREFIX, ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_PROCESS_CANDIDATE_GROUPS,
          BpmnXMLUtil.convertToDelimitedString(process.getCandidateStarterGroups()));
    }

    // write custom attributes
    BpmnXMLUtil.writeAttribute(process.getAttributes().values(), xtw, defaultAttributes);

    if (StringUtils.isNotEmpty(process.getDocumentation())) {

      xtw.writeStartElement(ELEMENT_DOCUMENTATION);
      xtw.writeCharacters(process.getDocumentation());
      xtw.writeEndElement();
    }
    
    boolean didWriteExtensionStartElement = ActivitiListenerExport.writeListeners(process, false, xtw);
    didWriteExtensionStartElement = BpmnXMLUtil.writeExtensionElements(process, didWriteExtensionStartElement, xtw);
    
    if (didWriteExtensionStartElement) {
      // closing extensions element
      xtw.writeEndElement();
    }
    
    LaneExport.writeLanes(process, xtw);
  }
}
