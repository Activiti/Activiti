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

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.TimerEventDefinition;

/**
 * @author Tijs Rademakers
 */
public class TimerEventDefinitionParser extends BaseChildElementParser {

  public String getElementName() {
    return "timerEventDefinition";
  }
  
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement) throws Exception {
    if (parentElement instanceof Event == false) return;
    
    TimerEventDefinition eventDefinition = new TimerEventDefinition();
    try {
      while (xtr.hasNext()) {
        xtr.next();
        if (xtr.isStartElement() && "timeDuration".equalsIgnoreCase(xtr.getLocalName())) {
          eventDefinition.setTimeDuration(xtr.getElementText());
          break;

        } else if (xtr.isStartElement() && "timeDate".equalsIgnoreCase(xtr.getLocalName())) {
          eventDefinition.setTimeDate(xtr.getElementText());
          break;

        } else if (xtr.isStartElement() && "timeCycle".equalsIgnoreCase(xtr.getLocalName())) {
          eventDefinition.setTimeCycle(xtr.getElementText());
          break;
          
        } else if (xtr.isEndElement() && "timerEventDefinition".equalsIgnoreCase(xtr.getLocalName())) {
          break;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Error parsing timer event definition", e);
    }
    
    ((Event) parentElement).getEventDefinitions().add(eventDefinition);
  }
}
