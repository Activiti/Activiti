package org.activiti.engine.repository;

import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti.engine.repository.ProcessDefinition}s via native (SQL) queries
 * 

 */
public interface NativeProcessDefinitionQuery extends NativeQuery<NativeProcessDefinitionQuery, ProcessDefinition> {

}