package org.activiti.services.audit.mongo.impl;

import java.util.List;

import org.activiti.services.audit.mongo.EventsMongoRepository;
import org.activiti.services.audit.mongo.entity.EventLogDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class EventsMongoRepositoryImpl implements EventsMongoRepository {

    private final static String COLLECTION_NAME = "act_evt_log";

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public void save(final EventLogDocument message) {
        mongoTemplate.save(message, COLLECTION_NAME);
    }

    @Override
    public void insertAll(List<EventLogDocument> messageList) {
        BulkOperations ops = mongoTemplate.bulkOps(BulkMode.ORDERED, COLLECTION_NAME);
        ops.insert(messageList);
        ops.execute();
    }

    @Override
    public EventLogDocument findById(final String id) {
        return mongoTemplate.findById(id, EventLogDocument.class, COLLECTION_NAME);
    }

    @Override
    public List<EventLogDocument> findAll() {
        return mongoTemplate.findAll(EventLogDocument.class, COLLECTION_NAME);
    }

    @Override
    public Page<EventLogDocument> findAll(Pageable pageable) {
        long total = mongoTemplate.count(new Query(), EventLogDocument.class, COLLECTION_NAME);
        List<EventLogDocument> content = null;
        if (pageable != null) {
            Query query = new Query();
            query.with(pageable);
            content = mongoTemplate.find(query, EventLogDocument.class, COLLECTION_NAME);
        } else {
            content = mongoTemplate.find(new Query(), EventLogDocument.class, COLLECTION_NAME);
        }
        return new PageImpl<EventLogDocument>(content, pageable, total);
    }

    @Override
    public void deleteAll() {
        mongoTemplate.remove(new Query(), COLLECTION_NAME);
    }

}
