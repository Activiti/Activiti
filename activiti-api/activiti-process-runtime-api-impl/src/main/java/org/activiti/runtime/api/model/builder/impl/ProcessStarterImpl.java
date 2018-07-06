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

package org.activiti.runtime.api.model.builder.impl;

import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.runtime.api.model.FluentProcessInstance;
import org.activiti.runtime.api.model.builder.ProcessStarter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;

public class ProcessStarterImpl implements ProcessStarter {

    private final StartProcessPayloadImpl payload;

    private final RuntimeService runtimeService;

    private final APIProcessInstanceConverter processInstanceConverter;

    public ProcessStarterImpl(String processDefinitionId,
                              RuntimeService runtimeService,
                              APIProcessInstanceConverter processInstanceConverter) {
        this.runtimeService = runtimeService;
        this.processInstanceConverter = processInstanceConverter;
        payload = new StartProcessPayloadImpl(processDefinitionId);
    }

    public ProcessStarterImpl processDefinitionId(String processDefinitionId){
        payload.setProcessDefinitionId(processDefinitionId);
        return this;
    }

    public ProcessStarterImpl variables(Map<String, Object> variables){
        payload.setVariables(variables);
        return this;
    }

    @Override
    public ProcessStarter variable(String key,
                                       Object value) {
        payload.addVariable(key, value);
        return this;
    }

    public ProcessStarterImpl businessKey(String businessKey){
        payload.setBusinessKey(businessKey);
        return this;
    }

    @Override
    public FluentProcessInstance doIt() {
        return processInstanceConverter.from(runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionId(payload.getProcessDefinitionId())
                .businessKey(payload.getBusinessKey())
                .variables(payload.getVariables())
                .start());
    }

}
