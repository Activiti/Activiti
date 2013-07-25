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
package org.activiti.bpmn.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BaseElement;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class AssociationXMLConverter extends BaseBpmnXMLConverter {
  
  public static String getXMLType() {
    return ELEMENT_ASSOCIATION;
  }
  
  public static Class<? extends BaseElement> getBpmnElementType() {
    return Association.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_ASSOCIATION;
  }
  
  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
    Association association = new Association();
    BpmnXMLUtil.addXMLLocation(association, xtr);
    association.setSourceRef(xtr.getAttributeValue(null, ATTRIBUTE_FLOW_SOURCE_REF));
    association.setTargetRef(xtr.getAttributeValue(null, ATTRIBUTE_FLOW_TARGET_REF));
    association.setId(xtr.getAttributeValue(null, ATTRIBUTE_ID));
    
    if(StringUtils.isEmpty(association.getSourceRef())) {
      model.addProblem("association element missing attribute 'sourceRef'", xtr);
    }
    if(StringUtils.isEmpty(association.getTargetRef())) {
      model.addProblem("association element missing attribute 'targetRef'", xtr);
    }
    
    return association;
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
    Association association = (Association) element;
    writeDefaultAttribute(ATTRIBUTE_FLOW_SOURCE_REF, association.getSourceRef(), xtw);
    writeDefaultAttribute(ATTRIBUTE_FLOW_TARGET_REF, association.getTargetRef(), xtw);
  }

  @Override
  protected void writeExtensionChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }
  
  @Override
  protected void writeAdditionalChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }
}
