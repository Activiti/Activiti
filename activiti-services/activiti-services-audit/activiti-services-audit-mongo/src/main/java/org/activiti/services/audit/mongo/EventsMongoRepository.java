package org.activiti.services.audit.mongo;

import java.util.List;
import java.util.Optional;

import org.activiti.services.audit.mongo.events.ProcessEngineEventDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface EventsMongoRepository extends MongoRepository<ProcessEngineEventDocument, String>, QuerydslPredicateExecutor<ProcessEngineEventDocument> {

    @Override
    public Optional<ProcessEngineEventDocument> findById(String id);

    @Override
    public List<ProcessEngineEventDocument> findAll();

    @Override
    public Page<ProcessEngineEventDocument> findAll(Pageable pageable);

    @Override
    public void deleteAll();
}
