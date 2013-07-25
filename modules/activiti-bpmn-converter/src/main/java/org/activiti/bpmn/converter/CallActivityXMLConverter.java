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

import java.util.List;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.child.BaseChildElementParser;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.IOParameter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class CallActivityXMLConverter extends BaseBpmnXMLConverter {
  
  public CallActivityXMLConverter() {
  	InParameterParser inParameterParser = new InParameterParser();
    childElementParsers.put(inParameterParser.getElementName(), inParameterParser);
    OutParameterParser outParameterParser = new OutParameterParser();
    childElementParsers.put(outParameterParser.getElementName(), outParameterParser);
  }

  public static String getXMLType() {
    return ELEMENT_CALL_ACTIVITY;
  }
  
  public static Class<? extends BaseElement> getBpmnElementType() {
    return CallActivity.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_CALL_ACTIVITY;
  }
  
  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
    CallActivity callActivity = new CallActivity();
    BpmnXMLUtil.addXMLLocation(callActivity, xtr);
    callActivity.setCalledElement(xtr.getAttributeValue(null, ATTRIBUTE_CALL_ACTIVITY_CALLEDELEMENT));
    parseChildElements(getXMLElementName(), callActivity, xtr);
    return callActivity;
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
    CallActivity callActivity = (CallActivity) element;
    if (StringUtils.isNotEmpty(callActivity.getCalledElement())) {
      xtw.writeAttribute(ATTRIBUTE_CALL_ACTIVITY_CALLEDELEMENT, callActivity.getCalledElement());
    }
  }
  
  @Override
  protected void writeExtensionChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
    CallActivity callActivity = (CallActivity) element;
    writeIOParameters(ELEMENT_CALL_ACTIVITY_IN_PARAMETERS, callActivity.getInParameters(), xtw);
    writeIOParameters(ELEMENT_CALL_ACTIVITY_OUT_PARAMETERS, callActivity.getOutParameters(), xtw);
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }
  
  private void writeIOParameters(String elementName, List<IOParameter> parameterList, XMLStreamWriter xtw) throws Exception {
    if (parameterList.size() == 0) return;
    
    for (IOParameter ioParameter : parameterList) {
      if (didWriteExtensionStartElement == false) { 
        xtw.writeStartElement(ELEMENT_EXTENSIONS);
        didWriteExtensionStartElement = true;
      }
      
      xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, elementName, ACTIVITI_EXTENSIONS_NAMESPACE);
      if (StringUtils.isNotEmpty(ioParameter.getSource())) {
        writeDefaultAttribute(ATTRIBUTE_IOPARAMETER_SOURCE, ioParameter.getSource(), xtw);
      }
      if (StringUtils.isNotEmpty(ioParameter.getSourceExpression())) {
        writeDefaultAttribute(ATTRIBUTE_IOPARAMETER_SOURCE_EXPRESSION, ioParameter.getSourceExpression(), xtw);
      }
      if (StringUtils.isNotEmpty(ioParameter.getTarget())) {
        writeDefaultAttribute(ATTRIBUTE_IOPARAMETER_TARGET, ioParameter.getTarget(), xtw);
      }
      
      xtw.writeEndElement();
    }
  }
  
  public class InParameterParser extends BaseChildElementParser {

    public String getElementName() {
      return ELEMENT_CALL_ACTIVITY_IN_PARAMETERS;
    }

    public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
      String source = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_SOURCE);
      String sourceExpression = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_SOURCE_EXPRESSION);
      String target = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_TARGET);
      if((StringUtils.isNotEmpty(source) || StringUtils.isNotEmpty(sourceExpression)) && StringUtils.isNotEmpty(target)) {
        
        IOParameter parameter = new IOParameter();
        if(StringUtils.isNotEmpty(sourceExpression)) {
          parameter.setSourceExpression(sourceExpression);
        } else {
          parameter.setSource(source);
        }
        
        parameter.setTarget(target);
        
        ((CallActivity) parentElement).getInParameters().add(parameter);
      }
    }
  }
  
  public class OutParameterParser extends BaseChildElementParser {

    public String getElementName() {
      return ELEMENT_CALL_ACTIVITY_OUT_PARAMETERS;
    }

    public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
      String source = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_SOURCE);
      String sourceExpression = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_SOURCE_EXPRESSION);
      String target = xtr.getAttributeValue(null, ATTRIBUTE_IOPARAMETER_TARGET);
      if((StringUtils.isNotEmpty(source) || StringUtils.isNotEmpty(sourceExpression)) && StringUtils.isNotEmpty(target)) {
        
        IOParameter parameter = new IOParameter();
        if(StringUtils.isNotEmpty(sourceExpression)) {
          parameter.setSourceExpression(sourceExpression);
        } else {
          parameter.setSource(source);
        }
        
        parameter.setTarget(target);
        
        ((CallActivity) parentElement).getOutParameters().add(parameter);
      }
    }
  }
}
