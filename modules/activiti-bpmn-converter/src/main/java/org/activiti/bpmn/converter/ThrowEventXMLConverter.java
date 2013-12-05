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
import org.activiti.bpmn.model.ThrowEvent;

/**
 * @author Tijs Rademakers
 */
public class ThrowEventXMLConverter extends BaseBpmnXMLConverter {
  
  public static String getXMLType() {
    return ELEMENT_EVENT_THROW;
  }
  
  public static Class<? extends BaseElement> getBpmnElementType() {
    return ThrowEvent.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_EVENT_THROW;
  }
  
  @Override
  protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
    ThrowEvent throwEvent = new ThrowEvent();
    BpmnXMLUtil.addXMLLocation(throwEvent, xtr);
    parseChildElements(getXMLElementName(), throwEvent, xtr);
    return throwEvent;
  }

  @Override
  protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }
  
  @Override
  protected void writeExtensionChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
  }

  @Override
  protected void writeAdditionalChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
    ThrowEvent throwEvent = (ThrowEvent) element;
    writeEventDefinitions(throwEvent, throwEvent.getEventDefinitions(), xtw);
  }
}
