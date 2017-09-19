package org.activiti.services.query.es.repository;

import org.activiti.services.query.es.model.ProcessInstanceES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProcessInstanceRepository extends ElasticsearchRepository<ProcessInstanceES, Long> {

}
