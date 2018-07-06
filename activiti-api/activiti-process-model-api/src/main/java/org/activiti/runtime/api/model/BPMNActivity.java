package org.activiti.runtime.api.model;

public interface BPMNActivity extends BPMNElement {

    String getActivityName();

    String getActivityType();

    String getElementId();
}
