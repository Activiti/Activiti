package org.activiti.engine.impl.runtime;


import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class MoveActivityIdContainer {

    protected List<String> activityIds;
    protected List<String> moveToActivityIds;
    protected boolean moveToParentProcess;
    protected boolean moveToSubProcessInstance;
    protected String callActivityId;
    protected Integer callActivitySubProcessVersion;
    protected String newAssigneeId;

    public MoveActivityIdContainer(String singleActivityId, String moveToActivityId) {
        this(singleActivityId, moveToActivityId, null);
    }

    public MoveActivityIdContainer(String singleActivityId, String moveToActivityId, String newAssigneeId) {
        this.activityIds = Collections.singletonList(singleActivityId);
        this.moveToActivityIds = Collections.singletonList(moveToActivityId);
        this.newAssigneeId = newAssigneeId;
    }

    public MoveActivityIdContainer(List<String> activityIds, String moveToActivityId) {
        this(activityIds, moveToActivityId, null);
    }

    public MoveActivityIdContainer(List<String> activityIds, String moveToActivityId, String newAssigneeId) {
        this.activityIds = activityIds;
        this.moveToActivityIds = Collections.singletonList(moveToActivityId);
        this.newAssigneeId = newAssigneeId;
    }

    public MoveActivityIdContainer(String singleActivityId, List<String> moveToActivityIds) {
        this(singleActivityId, moveToActivityIds, null);
    }

    public MoveActivityIdContainer(String singleActivityId, List<String> moveToActivityIds, String newAssigneeId) {
        this.activityIds = Collections.singletonList(singleActivityId);
        this.moveToActivityIds = moveToActivityIds;
        this.newAssigneeId = newAssigneeId;
    }

    public List<String> getActivityIds() {
        return Optional.ofNullable(activityIds).orElse(Collections.emptyList());
    }

    public List<String> getMoveToActivityIds() {
        return Optional.ofNullable(moveToActivityIds).orElse(Collections.emptyList());
    }

    public boolean isMoveToParentProcess() {
        return moveToParentProcess;
    }

    public void setMoveToParentProcess(boolean moveToParentProcess) {
        this.moveToParentProcess = moveToParentProcess;
    }

    public boolean isMoveToSubProcessInstance() {
        return moveToSubProcessInstance;
    }

    public void setMoveToSubProcessInstance(boolean moveToSubProcessInstance) {
        this.moveToSubProcessInstance = moveToSubProcessInstance;
    }

    public String getCallActivityId() {
        return callActivityId;
    }

    public void setCallActivityId(String callActivityId) {
        this.callActivityId = callActivityId;
    }

    public Integer getCallActivitySubProcessVersion() {
        return callActivitySubProcessVersion;
    }

    public void setCallActivitySubProcessVersion(Integer callActivitySubProcessVersion) {
        this.callActivitySubProcessVersion = callActivitySubProcessVersion;
    }

    public Optional<String> getNewAssigneeId() {
        return Optional.ofNullable(newAssigneeId);
    }
}
