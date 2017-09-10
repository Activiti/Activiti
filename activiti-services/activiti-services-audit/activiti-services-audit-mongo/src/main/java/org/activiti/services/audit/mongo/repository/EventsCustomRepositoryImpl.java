package org.activiti.services.audit.mongo.repository;

import org.activiti.services.audit.mongo.events.ProcessEngineEventDocument;
import org.activiti.services.audit.mongo.repository.EventsCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventsCustomRepositoryImpl implements EventsCustomRepository {

    private final static String COLLECTION_NAME = "act_evt_log";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void insertAll(ProcessEngineEventDocument[] events) {
        BulkOperations ops = mongoTemplate.bulkOps(BulkMode.ORDERED, COLLECTION_NAME);
        for (ProcessEngineEventDocument event : events) {
            ops.insert(event);
        }
        ops.execute();
    }

}
