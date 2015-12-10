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

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class UserTaskInfoMapper extends AbstractInfoMapper {

	protected void mapProperties(Object element) {
		UserTask userTask = (UserTask) element;
		createPropertyNode("Assignee", userTask.getAssignee());
		createPropertyNode("Candidate users", userTask.getCandidateUsers());
		createPropertyNode("Candidate groups", userTask.getCandidateGroups());
		createPropertyNode("Due date", userTask.getDueDate());
		createPropertyNode("Form key", userTask.getFormKey());
		createPropertyNode("Priority", userTask.getPriority());
		if (CollectionUtils.isNotEmpty(userTask.getFormProperties())) {
		    List<String> formPropertyValues = new ArrayList<String>();
		    for (FormProperty formProperty : userTask.getFormProperties()) {
		        StringBuilder propertyBuilder = new StringBuilder();
		        if (StringUtils.isNotEmpty(formProperty.getName())) {
		            propertyBuilder.append(formProperty.getName());
		        } else {
		            propertyBuilder.append(formProperty.getId());
		        }
		        if (StringUtils.isNotEmpty(formProperty.getType())) {
		            propertyBuilder.append(" - ");
		            propertyBuilder.append(formProperty.getType());
		        }
		        if (formProperty.isRequired()) {
		            propertyBuilder.append(" (required)");
		        } else {
		            propertyBuilder.append(" (not required)");
		        }
                formPropertyValues.add(propertyBuilder.toString());
            }
		    createPropertyNode("Form properties", formPropertyValues);
		}
		createListenerPropertyNodes("Task listeners", userTask.getTaskListeners());
		createListenerPropertyNodes("Execution listeners", userTask.getExecutionListeners());
	}
}
