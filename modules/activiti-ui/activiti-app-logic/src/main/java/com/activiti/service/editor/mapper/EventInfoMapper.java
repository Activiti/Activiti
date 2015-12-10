/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.service.editor.mapper;

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
