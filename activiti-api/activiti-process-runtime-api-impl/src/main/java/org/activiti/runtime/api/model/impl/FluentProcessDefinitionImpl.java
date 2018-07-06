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

package org.activiti.runtime.api.model.impl;

import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.runtime.api.model.FluentProcessDefinition;
import org.activiti.runtime.api.model.FluentProcessInstance;
import org.activiti.runtime.api.model.builder.ProcessStarter;
import org.activiti.runtime.api.model.builder.impl.ProcessStarterFactory;

public class FluentProcessDefinitionImpl extends ProcessDefinitionImpl implements FluentProcessDefinition {

    private final ProcessStarterFactory processStarterFactory;
    private final RuntimeService runtimeService;
    private final APIProcessInstanceConverter processInstanceConverter;

    public FluentProcessDefinitionImpl(ProcessStarterFactory processStarterFactory,
                                       RuntimeService runtimeService,
                                       APIProcessInstanceConverter processInstanceConverter) {
        this.processStarterFactory = processStarterFactory;
        this.runtimeService = runtimeService;
        this.processInstanceConverter = processInstanceConverter;
    }

    @Override
    public ProcessStarter startProcessWith() {
        return processStarterFactory.createNewInstance(getId());
    }

    @Override
    public FluentProcessInstance start() {
        return startProcessWith().doIt();
    }

    @Override
    public List<FluentProcessInstance> processInstances(int startIndex,
                                                        int maxResults) {
        return processInstanceConverter.from(runtimeService
                .createProcessInstanceQuery()
                .processDefinitionId(getId())
                .listPage(startIndex, maxResults));
    }
}