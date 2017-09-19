package org.activiti.services.query.es.repository;

import org.activiti.services.query.es.model.TaskES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TaskRepository extends ElasticsearchRepository<TaskES, Long> {

}
