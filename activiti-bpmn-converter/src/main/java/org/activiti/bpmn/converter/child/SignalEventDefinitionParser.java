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
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.apache.commons.lang3.StringUtils;

public class SignalEventDefinitionParser extends BaseChildElementParser {

    public String getElementName() {
        return ELEMENT_EVENT_SIGNALDEFINITION;
    }

    public void parseChildElement(XMLStreamReader xtr,
                                  BaseElement parentElement,
                                  BpmnModel model) throws Exception {
        if (!(parentElement instanceof Event)) {
            return;
        }

        SignalEventDefinition eventDefinition = new SignalEventDefinition();
        BpmnXMLUtil.addXMLLocation(eventDefinition,
                                   xtr);
        eventDefinition.setSignalRef(xtr.getAttributeValue(null,
                                                           ATTRIBUTE_SIGNAL_REF));
        eventDefinition.setSignalExpression(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE,
                                                                  ATTRIBUTE_SIGNAL_EXPRESSION));
        if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE,
                                                         ATTRIBUTE_ACTIVITY_ASYNCHRONOUS))) {
            eventDefinition.setAsync(Boolean.parseBoolean(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE,
                                                                                ATTRIBUTE_ACTIVITY_ASYNCHRONOUS)));
        }

        BpmnXMLUtil.parseChildElements(ELEMENT_EVENT_SIGNALDEFINITION,
                                       eventDefinition,
                                       xtr,
                                       model);

        ((Event) parentElement).getEventDefinitions().add(eventDefinition);
    }
}
