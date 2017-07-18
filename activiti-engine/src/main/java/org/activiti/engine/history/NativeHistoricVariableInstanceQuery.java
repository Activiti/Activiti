package org.activiti.engine.history;

import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti.engine.history.HistoricVariableInstance}s via native (SQL) queries
 * 

 */
public interface NativeHistoricVariableInstanceQuery extends NativeQuery<NativeHistoricVariableInstanceQuery, HistoricVariableInstance> {

}