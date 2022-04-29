/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.impl.util.ProcessInstanceHelper;
import org.activiti.engine.runtime.ProcessInstance;

public class StartCreatedProcessInstanceCmd<T> implements Command<ProcessInstance>, Serializable {

    private static final long serialVersionUID = 1L;
    private ProcessInstance internalProcessInstance;
    private Map<String, Object> variables;

    public StartCreatedProcessInstanceCmd(ProcessInstance internalProcessInstance, Map<String, Object> variables){
        this.internalProcessInstance = internalProcessInstance;
        this.variables = variables;
    }

    @Override
    public ProcessInstance execute(CommandContext commandContext) {
        if(this.internalProcessInstance.getStartTime() != null){
            throw new ActivitiIllegalArgumentException("Process instance " + this.internalProcessInstance.getProcessInstanceId() + " has already been started");
        }

        ExecutionEntity processExecution = (ExecutionEntity) internalProcessInstance;
        ProcessInstanceHelper processInstanceHelper = commandContext.getProcessEngineConfiguration().getProcessInstanceHelper();
        Process process = ProcessDefinitionUtil.getProcess(internalProcessInstance.getProcessDefinitionId());
        processInstanceHelper.startProcessInstance(processExecution, commandContext, variables,
            process.getInitialFlowElement(), Collections.emptyMap());
        return processExecution;
    }


}
