package org.activiti.api.process.model;


public interface StartMessageDeploymentDefinition {

    ProcessDefinition getProcessDefinition();

    StartMessageSubscription getMessageSubscription();
}
