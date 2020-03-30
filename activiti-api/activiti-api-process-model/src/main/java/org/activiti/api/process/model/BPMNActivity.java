package org.activiti.api.process.model;

public interface BPMNActivity extends BPMNElement {

    String getActivityName();

    String getActivityType();

    String getExecutionId();

}
