package org.activiti.api.runtime.model.impl;

import java.util.Objects;

import org.activiti.api.process.model.BPMNActivity;

public class BPMNActivityImpl extends BPMNElementImpl implements BPMNActivity {

    private String activityName;
    private String activityType;
    private String executionId;

    public BPMNActivityImpl() {
    }

    public BPMNActivityImpl(String elementId,
                            String activityName,
                            String activityType) {
        this.setElementId(elementId);
        this.activityName = activityName;
        this.activityType = activityType;
    }

    @Override
    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    @Override
    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    @Override
    public String getExecutionId() {
        return this.executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BPMNActivityImpl other = (BPMNActivityImpl) obj;
        return Objects.equals(activityName, other.activityName) &&
               Objects.equals(activityType, other.activityType) &&
               Objects.equals(executionId, other.executionId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(activityName, activityType, executionId);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BPMNActivityImpl [activityName=")
               .append(activityName)
               .append(", activityType=")
               .append(activityType)
               .append(", executionId=")
               .append(executionId)
               .append(", toString()=")
               .append(super.toString())
               .append("]");
        return builder.toString();
    }
}
