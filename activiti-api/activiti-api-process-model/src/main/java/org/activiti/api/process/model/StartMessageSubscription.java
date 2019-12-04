package org.activiti.api.process.model;

import java.util.Date;

public interface StartMessageSubscription {

    String getId();

    String getEventName();

    String getConfiguration();

    String getActivityId();

    Date getCreated();

    String getProcessDefinitionId();

}
