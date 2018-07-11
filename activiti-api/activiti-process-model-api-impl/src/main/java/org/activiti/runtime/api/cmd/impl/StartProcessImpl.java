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

package org.activiti.runtime.api.cmd.impl;

import java.util.Map;

import org.activiti.runtime.api.cmd.ProcessCommands;
import org.activiti.runtime.api.cmd.StartProcess;

public class StartProcessImpl extends CommandImpl<ProcessCommands> implements StartProcess {

    private String processDefinitionKey;
    private String processDefinitionId;
    private Map<String, Object> variables;
    private String businessKey;

    public StartProcessImpl() {
        super();
    }

    public StartProcessImpl(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public StartProcessImpl(String processDefinitionId,
                            Map<String, Object> variables) {
        this(processDefinitionId);
        this.variables = variables;
    }

    @Override
    public String getProcessInstanceId() {
        return null; // we don't need the process definition id for starting a new process
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    @Override
    public ProcessCommands getCommandType() {
        return ProcessCommands.START_PROCESS;
    }
}
