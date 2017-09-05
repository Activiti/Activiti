package org.activiti.services.audit.mongo;

import java.util.List;

import org.activiti.services.audit.mongo.entity.EventLogDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventsMongoRepository {

    public void save(final EventLogDocument message);

    public void insertAll(final List<EventLogDocument> messageList);

    public EventLogDocument findById(final String json);

    public List<EventLogDocument> findAll();

    public Page<EventLogDocument> findAll(Pageable pageable);

    public void deleteAll();
}
