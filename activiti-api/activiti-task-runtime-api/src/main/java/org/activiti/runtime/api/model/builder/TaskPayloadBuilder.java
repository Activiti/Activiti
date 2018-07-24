package org.activiti.runtime.api.model.builder;

public class TaskPayloadBuilder {

    public static GetTasksPayloadBuilder tasks() {
        return new GetTasksPayloadBuilder();
    }

    public static CompleteTaskPayloadBuilder complete() {
        return new CompleteTaskPayloadBuilder();
    }

    public static ClaimTaskPayloadBuilder claim() {
        return new ClaimTaskPayloadBuilder();
    }

    public static ReleaseTaskPayloadBuilder release() {
        return new ReleaseTaskPayloadBuilder();
    }

    public static SetTaskVariablesPayloadBuilder setVariables() {
        return new SetTaskVariablesPayloadBuilder();
    }

    public static GetTaskVariablesPayloadBuilder variables() {
        return new GetTaskVariablesPayloadBuilder();
    }

    public static UpdateTaskPayloadBuilder update() {
        return new UpdateTaskPayloadBuilder();
    }

    public static DeleteTaskPayloadBuilder delete() {
        return new DeleteTaskPayloadBuilder();
    }
}
