package org.activiti.runtime.api.model.builder;

public class PayloadBuilder {

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

    public static RemoveVariablesPayloadBuilder removeVariables() {
        return new RemoveVariablesPayloadBuilder();
    }

    public static SignalPayloadBuilder signal() {
        return new SignalPayloadBuilder();
    }
}
