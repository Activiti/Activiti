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
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.SequenceFlow;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class SequenceFlowXMLConverter extends BaseBpmnXMLConverter {
  
  public Class<? extends BaseElement> getBpmnElementType() {
    return SequenceFlow.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_SEQUENCE_FLOW;
  }
  
  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
    SequenceFlow sequenceFlow = new SequenceFlow();
    BpmnXMLUtil.addXMLLocation(sequenceFlow, xtr);
    sequenceFlow.setSourceRef(xtr.getAttributeValue(null, ATTRIBUTE_FLOW_SOURCE_REF));
    sequenceFlow.setTargetRef(xtr.getAttributeValue(null, ATTRIBUTE_FLOW_TARGET_REF));
    sequenceFlow.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
    sequenceFlow.setSkipExpression(xtr.getAttributeValue(null, ATTRIBUTE_FLOW_SKIP_EXPRESSION));
    
    parseChildElements(getXMLElementName(), sequenceFlow, model, xtr);
    
    return sequenceFlow;
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    SequenceFlow sequenceFlow = (SequenceFlow) element;
    writeDefaultAttribute(ATTRIBUTE_FLOW_SOURCE_REF, sequenceFlow.getSourceRef(), xtw);
    writeDefaultAttribute(ATTRIBUTE_FLOW_TARGET_REF, sequenceFlow.getTargetRef(), xtw);
    if (StringUtils.isNotEmpty(sequenceFlow.getSkipExpression())) {
      writeDefaultAttribute(ATTRIBUTE_FLOW_SKIP_EXPRESSION, sequenceFlow.getSkipExpression(), xtw);
    }
  }
  
  @Override
  protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    SequenceFlow sequenceFlow = (SequenceFlow) element;
    
    if (StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())) {
      xtw.writeStartElement(ELEMENT_FLOW_CONDITION);
      xtw.writeAttribute(XSI_PREFIX, XSI_NAMESPACE, "type", "tFormalExpression");
      xtw.writeCData(sequenceFlow.getConditionExpression());
      xtw.writeEndElement();
    }
  }
}
