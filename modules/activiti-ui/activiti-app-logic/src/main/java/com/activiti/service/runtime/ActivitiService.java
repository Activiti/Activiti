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
package com.activiti.service.runtime;

import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wrapper for various Activiti operations
 * 
 * @author jbarrez
 */
@Service
@Transactional
public class ActivitiService {
	
	@Autowired
	private RuntimeService runtimeService;
	
	public ProcessInstance startProcessInstance(String processDefinitionId, Map<String, Object> variables, String processInstanceName) {
		
		 // Actually start the process
        // No need to pass the tenant id here, the process definition is already tenant based and the process instance will inherit it
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, variables);
        
        // Can only set name in case process didn't end instantly
        if (!processInstance.isEnded() && processInstanceName != null) {
            runtimeService.setProcessInstanceName(processInstance.getId(), processInstanceName);
        }
        
        return processInstance;
        
	}

}
