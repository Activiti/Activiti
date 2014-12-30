package org.activiti5.engine.history;

import org.activiti5.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti5.engine.history.HistoricVariableInstance}s via native (SQL) queries
 * @author Henry Yan(http://www.kafeitu.me)
 */
public interface NativeHistoricVariableInstanceQuery
    extends NativeQuery<NativeHistoricVariableInstanceQuery, HistoricVariableInstance> {

}