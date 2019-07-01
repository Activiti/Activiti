/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.runtime.api.impl;

import java.util.Map;

import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.TaskVariableCopier;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.spring.process.ProcessExtensionService;

public class DefaultUserTaskBehavior extends UserTaskActivityBehavior {

    private ProcessExtensionService processExtensionService;
    private Map<String, Object> inboundVars = null;
  
    public DefaultUserTaskBehavior(UserTask userTask,
                                   ProcessExtensionService processExtensionService) {
        super(userTask);
        this.processExtensionService = processExtensionService;
    }

 
    @Override
    public void execute(DelegateExecution execution) {
        
        InboundVariablesMappingProvider MappingProvider = new InboundVariablesMappingProvider(processExtensionService);
        inboundVars = MappingProvider.calculateVariables(execution);
        
        super.execute(execution);
    }
    
    @Override
    public void setTaskVariables(CommandContext commandContext, TaskEntity task) {
        if (inboundVars == null) {
            super.setTaskVariables(commandContext, task);  
        } else {
            task.setVariablesLocal(inboundVars);
            
        }
       
        
    }

}
