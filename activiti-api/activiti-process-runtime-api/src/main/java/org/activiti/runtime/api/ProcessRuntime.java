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

import java.util.List;

import org.activiti.runtime.api.conf.ProcessRuntimeConfiguration;
import org.activiti.runtime.api.model.ProcessDefinition;
import org.activiti.runtime.api.model.ProcessDefinitionMeta;
import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.ProcessInstanceMeta;
import org.activiti.runtime.api.model.VariableInstance;
import org.activiti.runtime.api.model.payloads.DeleteProcessPayload;
import org.activiti.runtime.api.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.runtime.api.model.payloads.GetProcessInstancesPayload;
import org.activiti.runtime.api.model.payloads.GetVariablesPayload;
import org.activiti.runtime.api.model.payloads.RemoveProcessVariablesPayload;
import org.activiti.runtime.api.model.payloads.ResumeProcessPayload;
import org.activiti.runtime.api.model.payloads.SetProcessVariablesPayload;
import org.activiti.runtime.api.model.payloads.SignalPayload;
import org.activiti.runtime.api.model.payloads.StartProcessPayload;
import org.activiti.runtime.api.model.payloads.SuspendProcessPayload;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;

public interface ProcessRuntime {

    ProcessRuntimeConfiguration configuration();

    ProcessDefinition processDefinition(String processDefinitionKey);

    ProcessDefinitionMeta processDefinitionMeta(String processDefinitionKey);

    Page<ProcessDefinition> processDefinitions(Pageable pageable);

    Page<ProcessDefinition> processDefinitions(Pageable pageable,
                                               GetProcessDefinitionsPayload getProcessDefinitionsPayload);

    ProcessInstance processInstance(String processInstanceId);

    ProcessInstanceMeta processInstanceMeta(String processInstanceId);

    Page<ProcessInstance> processInstances(Pageable pageable);

    Page<ProcessInstance> processInstances(Pageable pageable,
                                           GetProcessInstancesPayload getProcessInstancesPayload);

    ProcessInstance start(StartProcessPayload startProcessPayload);

    ProcessInstance suspend(SuspendProcessPayload suspendProcessPayload);

    ProcessInstance resume(ResumeProcessPayload resumeProcessPayload);

    ProcessInstance delete(DeleteProcessPayload deleteProcessPayload);

    List<VariableInstance> variables(GetVariablesPayload getVariablesPayload); //I want to rename VariableInstance to Variable and it needs to be paged

    void removeVariables(RemoveProcessVariablesPayload removeProcessVariablesPayload); // review if we need to return removed variables// DO WE NEED THIS?>

    void signal(SignalPayload signalPayload);

    void setVariables(SetProcessVariablesPayload setProcessVariablesPayload); // review if we need to return set variables

}
