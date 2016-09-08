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
package org.activiti.app.service.editor.mapper;

import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class EventInfoMapper extends AbstractInfoMapper {

	protected void mapProperties(Object element) {
		Event event = (Event) element;
		if (CollectionUtils.isNotEmpty(event.getEventDefinitions())) {
		    EventDefinition eventDef = event.getEventDefinitions().get(0);
		    if (eventDef instanceof TimerEventDefinition) {
		        TimerEventDefinition timerDef = (TimerEventDefinition) eventDef;
		        if (StringUtils.isNotEmpty(timerDef.getTimeDate())) {
		            createPropertyNode("Timer date", timerDef.getTimeDate());
		        }
		        if (StringUtils.isNotEmpty(timerDef.getTimeDuration())) {
                    createPropertyNode("Timer duration", timerDef.getTimeDuration());
                }
		        if (StringUtils.isNotEmpty(timerDef.getTimeDuration())) {
                    createPropertyNode("Timer cycle", timerDef.getTimeCycle());
                }
		    
		    } else if (eventDef instanceof SignalEventDefinition) {
		        SignalEventDefinition signalDef = (SignalEventDefinition) eventDef;
		        if (StringUtils.isNotEmpty(signalDef.getSignalRef())) {
		            createPropertyNode("Signal ref", signalDef.getSignalRef());
		        }
		        
		    } else if (eventDef instanceof MessageEventDefinition) {
		        MessageEventDefinition messageDef = (MessageEventDefinition) eventDef;
                if (StringUtils.isNotEmpty(messageDef.getMessageRef())) {
                    createPropertyNode("Message ref", messageDef.getMessageRef());
                }
		    
		    } else if (eventDef instanceof ErrorEventDefinition) {
		        ErrorEventDefinition errorDef = (ErrorEventDefinition) eventDef;
                if (StringUtils.isNotEmpty(errorDef.getErrorCode())) {
                    createPropertyNode("Error code", errorDef.getErrorCode());
                }
		    }
		}
		createListenerPropertyNodes("Execution listeners", event.getExecutionListeners());
	}
}
