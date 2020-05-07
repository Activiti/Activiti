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

    public static CreateProcessPayloadBuilder create() { return new CreateProcessPayloadBuilder(); }

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
