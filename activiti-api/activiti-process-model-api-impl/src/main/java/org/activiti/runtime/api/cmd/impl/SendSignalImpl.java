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

import org.activiti.runtime.api.cmd.RuntimeCommands;
import org.activiti.runtime.api.cmd.SendSignal;

public class SendSignalImpl extends CommandImpl<RuntimeCommands> implements SendSignal {

    private String name;
    private Map<String, Object> inputVariables;

    public SendSignalImpl() {
    }

    public SendSignalImpl(String name) {
        this.name = name;
    }

    public SendSignalImpl(String name,
                          Map<String, Object> inputVariables) {
        this(name);
        this.inputVariables = inputVariables;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> getInputVariables() {
        return inputVariables;
    }

    @Override
    public RuntimeCommands getCommandType() {
        return RuntimeCommands.SEND_SIGNAL;
    }

}
