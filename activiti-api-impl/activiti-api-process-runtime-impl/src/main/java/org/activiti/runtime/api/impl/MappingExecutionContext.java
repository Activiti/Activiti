package org.activiti.runtime.api.impl;

import java.util.Objects;

import org.activiti.engine.delegate.DelegateExecution;

public class MappingExecutionContext {

    private String processDefinitionId;
    private String activityId;

    public MappingExecutionContext(DelegateExecution delegateExecution) {
        this.processDefinitionId = delegateExecution.getProcessDefinitionId();
        this.activityId = delegateExecution.getCurrentActivityId();
    }

    public MappingExecutionContext(String processDefinitionId,
                                   String activityId) {
        this.processDefinitionId = processDefinitionId;
        this.activityId = activityId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getActivityId() {
        return activityId;
    }

    public static MappingExecutionContext buildMappingExecutionContext(DelegateExecution delegateExecution) {
        return new MappingExecutionContext(delegateExecution);
    }

    public static MappingExecutionContext buildMappingExecutionContext(String processDefinitionId,
                                                                       String activityId) {
        return new MappingExecutionContext(processDefinitionId,
                activityId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MappingExecutionContext that = (MappingExecutionContext) o;
        return Objects.equals(processDefinitionId,
                that.processDefinitionId) &&
                Objects.equals(activityId,
                        that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processDefinitionId,
                activityId);
    }
}
