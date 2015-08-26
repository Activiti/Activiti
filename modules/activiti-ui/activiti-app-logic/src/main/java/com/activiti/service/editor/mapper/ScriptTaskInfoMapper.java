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

import org.activiti.bpmn.model.ScriptTask;
import org.apache.commons.lang3.StringUtils;

public class ScriptTaskInfoMapper extends AbstractInfoMapper {

	protected void mapProperties(Object element) {
		ScriptTask scriptTask = (ScriptTask) element;
		if (StringUtils.isNotEmpty(scriptTask.getScriptFormat())) {
		    createPropertyNode("Script format", scriptTask.getScriptFormat());
		}
		if (StringUtils.isNotEmpty(scriptTask.getScript())) {
            createPropertyNode("Script", scriptTask.getScript());
        }
		createListenerPropertyNodes("Execution listeners", scriptTask.getExecutionListeners());
	}
}
