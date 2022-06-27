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
package org.activiti.api.process.model.builders;


import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.CreateProcessInstancePayload;
import org.activiti.api.process.model.payloads.DeleteProcessPayload;
import org.activiti.api.process.model.payloads.GetProcessInstancesPayload;
import org.activiti.api.process.model.payloads.ResumeProcessPayload;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.process.model.payloads.SuspendProcessPayload;

public class ProcessPayloadBuilder {

    public static StartProcessPayloadBuilder start() {
        return new StartProcessPayloadBuilder();
    }

    public static CreateProcessPayloadBuilder create() {
        return new CreateProcessPayloadBuilder();
    }

    public static StartProcessPayloadBuilder start(StartProcessPayload from) {
        return new StartProcessPayloadBuilder().withBusinessKey(from.getBusinessKey())
                                               .withName(from.getName())
                                               .withProcessDefinitionId(from.getProcessDefinitionId())
                                               .withProcessDefinitionKey(from.getProcessDefinitionKey())
                                               .withVariables(from.getVariables());
    }

    public static CreateProcessPayloadBuilder create(CreateProcessInstancePayload from) {
        return new CreateProcessPayloadBuilder().withName(from.getName())
            .withProcessDefinitionId(from.getProcessDefinitionId())
            .withProcessDefinitionKey(from.getProcessDefinitionKey())
            .withBusinessKey(from.getBusinessKey());
    }

    public static DeleteProcessPayloadBuilder delete() {
        return new DeleteProcessPayloadBuilder();
    }

    public static SuspendProcessPayloadBuilder suspend() {
        return new SuspendProcessPayloadBuilder();
    }

    public static ResumeProcessPayloadBuilder resume() {
        return new ResumeProcessPayloadBuilder();
    }

    public static UpdateProcessPayloadBuilder update() {
        return new UpdateProcessPayloadBuilder();
    }

    public static GetVariablesPayloadBuilder variables() {
        return new GetVariablesPayloadBuilder();
    }

    public static SetVariablesPayloadBuilder setVariables() {
        return new SetVariablesPayloadBuilder();
    }

    public static SetVariablesPayloadBuilder setVariables(ProcessInstance processInstance) {
        return new SetVariablesPayloadBuilder(processInstance);
    }

    public static SetVariablesPayloadBuilder setVariables(String processInstanceId) {
        return new SetVariablesPayloadBuilder(processInstanceId);
    }

    public static RemoveVariablesPayloadBuilder removeVariables() {
        return new RemoveVariablesPayloadBuilder();
    }

    public static SignalPayloadBuilder signal() {
        return new SignalPayloadBuilder();
    }

    public static GetProcessDefinitionsPayloadBuilder processDefinitions() {
        return new GetProcessDefinitionsPayloadBuilder();
    }

    public static GetProcessInstancesPayloadBuilder processInstances() {
        return new GetProcessInstancesPayloadBuilder();
    }

    public static GetProcessInstancesPayload subprocesses(String parentProcessInstanceId) {
        return new GetProcessInstancesPayloadBuilder().withParentProcessInstanceId(parentProcessInstanceId).build();
    }

    public static GetProcessInstancesPayload subprocesses(ProcessInstance parentProcessInstance) {
        return new GetProcessInstancesPayloadBuilder().withParentProcessInstanceId(parentProcessInstance.getId()).build();
    }

    /* shortcuts - This needs to be justified and validated before adding any new one*/

    public static SuspendProcessPayload suspend(String processInstanceId) {
        return new SuspendProcessPayloadBuilder().withProcessInstanceId(processInstanceId).build();
    }

    public static SuspendProcessPayload suspend(ProcessInstance processInstance) {
        return new SuspendProcessPayloadBuilder().withProcessInstance(processInstance).build();
    }

    public static ResumeProcessPayload resume(String processInstanceId) {
        return new ResumeProcessPayloadBuilder().withProcessInstanceId(processInstanceId).build();
    }

    public static ResumeProcessPayload resume(ProcessInstance processInstance) {
        return new ResumeProcessPayloadBuilder().withProcessInstance(processInstance).build();
    }

    public static DeleteProcessPayload delete(String processInstanceId) {
        return new DeleteProcessPayloadBuilder().withProcessInstanceId(processInstanceId).build();
    }

    public static DeleteProcessPayload delete(ProcessInstance processInstance) {
        return new DeleteProcessPayloadBuilder().withProcessInstance(processInstance).build();
    }
}
