package org.activiti.runtime.api.model.builders;

import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.DeleteProcessPayload;
import org.activiti.runtime.api.model.payloads.ResumeProcessPayload;
import org.activiti.runtime.api.model.payloads.SuspendProcessPayload;

public class ProcessPayloadBuilder {

    public static StartProcessPayloadBuilder start() {
        return new StartProcessPayloadBuilder();
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

    public static GetVariablesPayloadBuilder variables() {
        return new GetVariablesPayloadBuilder();
    }

    public static SetVariablesPayloadBuilder setVariables() {
        return new SetVariablesPayloadBuilder();
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
