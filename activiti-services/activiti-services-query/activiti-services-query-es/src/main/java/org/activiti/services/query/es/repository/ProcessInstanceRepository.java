package org.activiti.services.query.es.repository;

import org.activiti.services.query.es.model.ProcessInstanceES;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

public class ProcessInstanceRepository extends ElasticsearchRepository<ProcessInstanceES, String> {
    
    Page<ProcessInstanceES> findAll(String id, Pageable pageable);


}
