package org.activiti.services.audit.mongo.impl;

import org.activiti.services.audit.mongo.EventsMongoCustomRepository;
import org.activiti.services.audit.mongo.events.ProcessEngineEventDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventsMongoCustomRepositoryImpl implements EventsMongoCustomRepository {

    private final static String COLLECTION_NAME = "act_evt_log";

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public void insertAll(ProcessEngineEventDocument[] events) {
        BulkOperations ops = mongoTemplate.bulkOps(BulkMode.ORDERED, COLLECTION_NAME);
        for (ProcessEngineEventDocument event : events) {
            ops.insert(event);
        }
        ops.execute();
    }

}
