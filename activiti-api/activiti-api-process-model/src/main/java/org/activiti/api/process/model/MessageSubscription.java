package org.activiti.api.process.model;

import java.util.Date;

public interface MessageSubscription {

    String getId();

    String getEventName();

    String getExecutionId();

    String getProcessInstanceId();
    
    String getBusinessKey();

    String getConfiguration();

    String getActivityId();

    Date getCreated();

    String getProcessDefinitionId();

}
