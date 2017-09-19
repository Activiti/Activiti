package org.activiti.services.query.es.repository;

import java.util.Optional;

import org.activiti.services.query.es.model.VariableES;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VariableRepository extends ElasticsearchRepository<VariableES, Long> {

	Page<VariableES> findAll(String processInstanceId, Pageable pageable);

	Optional<VariableES> findByName(String variableName);

	Optional<VariableES> findByProcessInstanceId(String variableName, String processInstanceId);

	Optional<VariableES> findByTaskId(String variableName, String taskId);

}
