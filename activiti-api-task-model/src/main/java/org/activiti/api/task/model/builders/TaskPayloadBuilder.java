package org.activiti.api.task.model.builders;

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

    public static CreateTaskPayloadBuilder create() {
        return new CreateTaskPayloadBuilder();
    }
    
    public static AssignTaskPayloadBuilder assign() {
        return new AssignTaskPayloadBuilder();
    }
    
    public static CandidateUsersPayloadBuilder addCandidateUsers() {
        return new CandidateUsersPayloadBuilder();
    }
    
    public static CandidateUsersPayloadBuilder deleteCandidateUsers() {
        return new CandidateUsersPayloadBuilder();
    }
    
    public static CandidateGroupsPayloadBuilder addCandidateGroups() {
        return new CandidateGroupsPayloadBuilder();
    }
    
    public static CandidateGroupsPayloadBuilder deleteCandidateGroups() {
        return new CandidateGroupsPayloadBuilder();
    }
}
