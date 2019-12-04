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
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.FieldExtension;
import org.apache.commons.lang3.StringUtils;

public class FieldExtensionExport implements BpmnXMLConstants {

  public static boolean writeFieldExtensions(List<FieldExtension> fieldExtensionList, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {

    for (FieldExtension fieldExtension : fieldExtensionList) {

      if (StringUtils.isNotEmpty(fieldExtension.getFieldName())) {

        if (StringUtils.isNotEmpty(fieldExtension.getStringValue()) || StringUtils.isNotEmpty(fieldExtension.getExpression())) {

          if (!didWriteExtensionStartElement) {
            xtw.writeStartElement(ELEMENT_EXTENSIONS);
            didWriteExtensionStartElement = true;
          }

          xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_FIELD, ACTIVITI_EXTENSIONS_NAMESPACE);
          BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_FIELD_NAME, fieldExtension.getFieldName(), xtw);

          if (StringUtils.isNotEmpty(fieldExtension.getStringValue())) {
            xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_FIELD_STRING, ACTIVITI_EXTENSIONS_NAMESPACE);
            xtw.writeCData(fieldExtension.getStringValue());
          } else {
            xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ATTRIBUTE_FIELD_EXPRESSION, ACTIVITI_EXTENSIONS_NAMESPACE);
            xtw.writeCData(fieldExtension.getExpression());
          }
          xtw.writeEndElement();
          xtw.writeEndElement();
        }
      }
    }
    return didWriteExtensionStartElement;
  }
}
