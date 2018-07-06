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

package org.activiti.runtime.api;

import org.activiti.runtime.api.conf.ProcessRuntimeConfiguration;
import org.activiti.runtime.api.model.FluentProcessDefinition;
import org.activiti.runtime.api.model.FluentProcessInstance;
import org.activiti.runtime.api.model.builder.SignalPayload;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.query.ProcessDefinitionFilter;
import org.activiti.runtime.api.query.ProcessInstanceFilter;

public interface ProcessRuntime {

    ProcessRuntimeConfiguration configuration();

    Page<FluentProcessDefinition> processDefinitions(Pageable pageable);

    Page<FluentProcessDefinition>  processDefinitions(Pageable pageable,
                                              ProcessDefinitionFilter filter);

    FluentProcessDefinition processDefinitionByKey(String processDefinitionKey);

    FluentProcessDefinition processDefinitionById(String processDefinitionId);

    FluentProcessInstance processInstance(String processInstanceId);

    Page<FluentProcessInstance> processInstances(Pageable pageable);

    Page<FluentProcessInstance> processInstances(Pageable pageable, ProcessInstanceFilter filter);

    SignalPayload sendSignalWith();

    void sendSignal(String name);

}
