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

import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;

public class ServiceTaskInfoMapper extends AbstractInfoMapper {

	protected void mapProperties(Object element) {
		ServiceTask serviceTask = (ServiceTask) element;
		if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(serviceTask.getImplementationType())) {
			createPropertyNode("Class", serviceTask.getImplementation());
		} else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(serviceTask.getImplementationType())) {
			createPropertyNode("Expression", serviceTask.getImplementation());
		} else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(serviceTask.getImplementationType())) {
			createPropertyNode("Delegate expression", serviceTask.getImplementation());
		}
		if (serviceTask.isAsynchronous()) {
		    createPropertyNode("Asynchronous", true);
		    createPropertyNode("Exclusive", !serviceTask.isNotExclusive());
		}
		if (ServiceTask.MAIL_TASK.equalsIgnoreCase(serviceTask.getType())) {
		    createPropertyNode("Type", "Mail task");
		}
		createPropertyNode("Result variable name", serviceTask.getResultVariableName());
		createFieldPropertyNodes("Field extensions", serviceTask.getFieldExtensions());
		createListenerPropertyNodes("Execution listeners", serviceTask.getExecutionListeners());
	}
}
