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

import java.util.List;

import org.activiti.runtime.api.cmd.ProcessCommands;
import org.activiti.runtime.api.cmd.RemoveProcessVariables;

public class RemoveProcessVariablesImpl extends CommandImpl<ProcessCommands> implements RemoveProcessVariables {

    private String processInstanceId;
    private List<String> variableNames;

    public RemoveProcessVariablesImpl() {
    }

    public RemoveProcessVariablesImpl(String processInstanceId,
                                      List<String> variableNames) {
        this.processInstanceId = processInstanceId;
        this.variableNames = variableNames;
    }

    @Override
    public List<String> getVariableNames() {
        return variableNames;
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    @Override
    public ProcessCommands getCommandType() {
        return ProcessCommands.REMOVE_PROCESS_VARIABLES;
    }
}
