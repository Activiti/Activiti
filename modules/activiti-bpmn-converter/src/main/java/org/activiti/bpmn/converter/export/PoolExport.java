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
import org.activiti.bpmn.converter.BaseBpmnXMLConverter;
import org.activiti.bpmn.converter.MessageFlowXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.MessageFlow;
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
      
      // write message flows
      BaseBpmnXMLConverter converter = new MessageFlowXMLConverter();
      for (MessageFlow messageFlow : model.getMessageFlows()) {
    	converter.convertToXML(xtw, messageFlow, model);  
      }
      
      xtw.writeEndElement();
    }
  }
}
