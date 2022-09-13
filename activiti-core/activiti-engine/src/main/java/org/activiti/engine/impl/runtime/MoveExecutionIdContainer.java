package org.activiti.engine.impl.runtime;


import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class MoveExecutionIdContainer {

    protected List<String> executionIds;
    protected List<String> moveToActivityIds;
    protected Optional<String> newAssigneeId;

    public MoveExecutionIdContainer(String singleExecutionId, String moveToActivityId) {
        this(singleExecutionId, moveToActivityId, null);
    }

    public MoveExecutionIdContainer(String singleExecutionId, String moveToActivityId, String newAssigneeId) {
        this.executionIds = Collections.singletonList(singleExecutionId);
        this.moveToActivityIds = Collections.singletonList(moveToActivityId);
        this.newAssigneeId = Optional.ofNullable(newAssigneeId);
    }

    public MoveExecutionIdContainer(List<String> executionIds, String moveToActivityId) {
        this(executionIds, moveToActivityId, null);

    }

    public MoveExecutionIdContainer(List<String> executionIds, String moveToActivityId, String newAssigneeId) {
        this.executionIds = executionIds;
        this.moveToActivityIds = Collections.singletonList(moveToActivityId);
        this.newAssigneeId = Optional.ofNullable(newAssigneeId);
    }

    public MoveExecutionIdContainer(String singleExecutionId, List<String> moveToActivityIds) {
        this(singleExecutionId, moveToActivityIds, null);
    }

    public MoveExecutionIdContainer(String singleExecutionId, List<String> moveToActivityIds, String newAssigneeId) {
        this.executionIds = Collections.singletonList(singleExecutionId);
        this.moveToActivityIds = moveToActivityIds;
        this.newAssigneeId = Optional.ofNullable(newAssigneeId);
    }

    public List<String> getExecutionIds() {
        return Optional.ofNullable(executionIds).orElse(Collections.emptyList());
    }

    public List<String> getMoveToActivityIds() {
        return Optional.ofNullable(moveToActivityIds).orElse(Collections.emptyList());
    }

    public Optional<String> getNewAssigneeId() {
        return newAssigneeId;
    }
}
