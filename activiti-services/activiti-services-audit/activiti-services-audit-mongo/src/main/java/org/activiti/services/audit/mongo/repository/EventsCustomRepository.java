package org.activiti.services.audit.mongo.repository;

import org.activiti.services.audit.mongo.events.ProcessEngineEventDocument;

public interface EventsCustomRepository {

    public void insertAll(final ProcessEngineEventDocument[] messageList);
}
