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
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.apache.commons.lang3.StringUtils;

public class MultiInstanceExport implements BpmnXMLConstants {

  public static void writeMultiInstance(Activity activity, XMLStreamWriter xtw) throws Exception {
    if (activity.getLoopCharacteristics() != null) {
      MultiInstanceLoopCharacteristics multiInstanceObject = activity.getLoopCharacteristics();
      if (hasMultiInstanceElements(multiInstanceObject)) {
          xtw.writeStartElement(ELEMENT_MULTIINSTANCE);
          BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_MULTIINSTANCE_SEQUENTIAL,
              String.valueOf(multiInstanceObject.isSequential()).toLowerCase(), xtw);
          if (StringUtils.isNotEmpty(multiInstanceObject.getInputDataItem())) {
              BpmnXMLUtil.writeQualifiedAttribute(ATTRIBUTE_MULTIINSTANCE_COLLECTION,
                  multiInstanceObject.getInputDataItem(), xtw);
          }
          if (StringUtils.isNotEmpty(multiInstanceObject.getElementVariable())) {
              BpmnXMLUtil.writeQualifiedAttribute(ATTRIBUTE_MULTIINSTANCE_VARIABLE,
                  multiInstanceObject.getElementVariable(), xtw);
          }
          if (StringUtils.isNotEmpty(multiInstanceObject.getLoopCardinality())) {
              xtw.writeStartElement(ELEMENT_MULTIINSTANCE_CARDINALITY);
              xtw.writeCharacters(multiInstanceObject.getLoopCardinality());
              xtw.writeEndElement();
          }
          if (StringUtils.isNotEmpty(multiInstanceObject.getLoopDataOutputRef())) {
              xtw.writeStartElement(ELEMENT_MULTI_INSTANCE_DATA_OUTPUT);
              xtw.writeCharacters(multiInstanceObject.getLoopDataOutputRef());
              xtw.writeEndElement();
          }
          if (StringUtils.isNotEmpty(multiInstanceObject.getOutputDataItem())) {
              xtw.writeStartElement(ELEMENT_MULTI_INSTANCE_OUTPUT_DATA_ITEM);
              xtw.writeAttribute(ATTRIBUTE_NAME, multiInstanceObject.getOutputDataItem());
              xtw.writeEndElement();
          }
          if (StringUtils.isNotEmpty(multiInstanceObject.getCompletionCondition())) {
              xtw.writeStartElement(ELEMENT_MULTIINSTANCE_CONDITION);
              xtw.writeCharacters(multiInstanceObject.getCompletionCondition());
              xtw.writeEndElement();
          }
          xtw.writeEndElement();
      }
    }
  }

    private static boolean hasMultiInstanceElements(
        MultiInstanceLoopCharacteristics multiInstanceObject) {
        return StringUtils.isNotEmpty(multiInstanceObject.getLoopCardinality()) ||
            StringUtils.isNotEmpty(multiInstanceObject.getInputDataItem()) ||
            StringUtils.isNotEmpty(multiInstanceObject.getCompletionCondition()) ||
            StringUtils.isNotEmpty(multiInstanceObject.getLoopDataOutputRef()) ||
            StringUtils.isNotEmpty(multiInstanceObject.getOutputDataItem());
    }
}
