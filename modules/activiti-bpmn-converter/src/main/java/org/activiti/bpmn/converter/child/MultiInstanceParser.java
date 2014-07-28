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
package org.activiti.bpmn.converter.child;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;

/**
 * @author Tijs Rademakers
 */
public class MultiInstanceParser extends BaseChildElementParser {

  public String getElementName() {
    return ELEMENT_MULTIINSTANCE;
  }
  
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
    if (parentElement instanceof Activity == false) return;
    
    MultiInstanceLoopCharacteristics multiInstanceDef = new MultiInstanceLoopCharacteristics();
    BpmnXMLUtil.addXMLLocation(multiInstanceDef, xtr);
    if (xtr.getAttributeValue(null, ATTRIBUTE_MULTIINSTANCE_SEQUENTIAL) != null) {
      multiInstanceDef.setSequential(Boolean.valueOf(xtr.getAttributeValue(null, ATTRIBUTE_MULTIINSTANCE_SEQUENTIAL)));
    }
    multiInstanceDef.setInputDataItem(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_MULTIINSTANCE_COLLECTION));
    multiInstanceDef.setElementVariable(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_MULTIINSTANCE_VARIABLE));
    multiInstanceDef.setElementIndexVariable(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_MULTIINSTANCE_INDEX_VARIABLE));

    boolean readyWithMultiInstance = false;
    try {
      while (readyWithMultiInstance == false && xtr.hasNext()) {
        xtr.next();
        if (xtr.isStartElement() && ELEMENT_MULTIINSTANCE_CARDINALITY.equalsIgnoreCase(xtr.getLocalName())) {
          multiInstanceDef.setLoopCardinality(xtr.getElementText());

        } else if (xtr.isStartElement() && ELEMENT_MULTIINSTANCE_DATAINPUT.equalsIgnoreCase(xtr.getLocalName())) {
          multiInstanceDef.setInputDataItem(xtr.getElementText());

        } else if (xtr.isStartElement() && ELEMENT_MULTIINSTANCE_DATAITEM.equalsIgnoreCase(xtr.getLocalName())) {
          if (xtr.getAttributeValue(null, ATTRIBUTE_NAME) != null) {
            multiInstanceDef.setElementVariable(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
          }

        } else if (xtr.isStartElement() && ELEMENT_MULTIINSTANCE_CONDITION.equalsIgnoreCase(xtr.getLocalName())) {
          multiInstanceDef.setCompletionCondition(xtr.getElementText());

        } else if (xtr.isEndElement() && getElementName().equalsIgnoreCase(xtr.getLocalName())) {
          readyWithMultiInstance = true;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Error parsing multi instance definition", e);
    }
    ((Activity) parentElement).setLoopCharacteristics(multiInstanceDef);
  }
}
