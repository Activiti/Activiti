package org.activiti.engine.history;

import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link HistoricTaskInstanceQuery}s via native (SQL) queries
 * @author Bernd Ruecker (camunda)
 */
public interface NativeHistoricTaskInstanceQuery extends NativeQuery<NativeHistoricTaskInstanceQuery, HistoricTaskInstance> {

}
