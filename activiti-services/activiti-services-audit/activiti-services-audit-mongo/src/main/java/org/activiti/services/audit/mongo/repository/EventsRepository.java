package org.activiti.services.audit.mongo.repository;

import org.activiti.services.audit.mongo.events.ProcessEngineEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface EventsRepository extends MongoRepository<ProcessEngineEventDocument, String>, QuerydslPredicateExecutor<ProcessEngineEventDocument> {
}
