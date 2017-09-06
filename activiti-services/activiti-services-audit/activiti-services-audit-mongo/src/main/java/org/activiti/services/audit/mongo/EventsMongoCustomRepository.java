package org.activiti.services.audit.mongo;

import org.activiti.services.audit.mongo.events.ProcessEngineEventDocument;

public interface EventsMongoCustomRepository {

    public void insertAll(final ProcessEngineEventDocument[] messageList);
}
