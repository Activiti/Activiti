package org.activiti.api.runtime.model.impl;

import java.util.Objects;

import org.activiti.api.process.model.BPMNActivity;

public class BPMNActivityImpl extends BPMNElementImpl implements BPMNActivity {

    private String activityName;
    private String activityType;


    public BPMNActivityImpl() {
    }

    public BPMNActivityImpl(String elementId,
                            String activityName,
                            String activityType) {
        this.setElementId(elementId);
        this.activityName = activityName;
        this.activityType = activityType;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BPMNActivityImpl that = (BPMNActivityImpl) o;

        return Objects.equals(getElementId(),
                that.getElementId()) &&
                Objects.equals(activityName,
                        that.activityName) &&
                Objects.equals(activityType,
                        that.activityType);
    }

    @Override
    public int hashCode() {

        return Objects.hash(getElementId(),
                activityName,
                activityType);
    }

    @Override
    public String toString() {
        return "BPMNActivityImpl{" +
                "activityName='" + activityName + '\'' +
                ", activityType='" + activityType + '\'' +
                ", elementId='" + getElementId() + '\'' +
                '}';
    }
}
