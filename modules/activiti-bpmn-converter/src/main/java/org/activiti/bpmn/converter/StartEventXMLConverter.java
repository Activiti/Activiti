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

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.alfresco.AlfrescoStartEvent;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class StartEventXMLConverter extends BaseBpmnXMLConverter {
  
  List<String> formTypes = new ArrayList<String>();
  
  public static String getXMLType() {
    return ELEMENT_EVENT_START;
  }
  
  public static Class<? extends BaseElement> getBpmnElementType() {
    return StartEvent.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_EVENT_START;
  }
  
  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
    String formKey = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_FORM_FORMKEY);
    StartEvent startEvent = null;
    if (StringUtils.isNotEmpty(formKey)) {
      if (formTypes.contains(formKey)) {
        startEvent = new AlfrescoStartEvent();
      }
    }
    if (startEvent == null) {
      startEvent = new StartEvent();
    }
    BpmnXMLUtil.addXMLLocation(startEvent, xtr);
    startEvent.setInitiator(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_EVENT_START_INITIATOR));
    startEvent.setFormKey(formKey);
    
    parseChildElements(getXMLElementName(), startEvent, xtr);
    
    return startEvent;
  }
  
  @Override
  protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
    StartEvent startEvent = (StartEvent) element;
    writeQualifiedAttribute(ATTRIBUTE_EVENT_START_INITIATOR, startEvent.getInitiator(), xtw);
    writeQualifiedAttribute(ATTRIBUTE_FORM_FORMKEY, startEvent.getFormKey(), xtw);
  }
  
  @Override
  protected void writeExtensionChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
    StartEvent startEvent = (StartEvent) element;
    writeFormProperties(startEvent, xtw);
  }
  
  @Override
  protected void writeAdditionalChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
    StartEvent startEvent = (StartEvent) element;
    writeEventDefinitions(startEvent, startEvent.getEventDefinitions(), xtw);
  }
  
  public void addFormType(String formType) {
    if (StringUtils.isNotEmpty(formType)) {
      formTypes.add(formType);
    }
  }
}
