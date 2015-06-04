package org.activiti5.engine.history;

import org.activiti5.engine.query.NativeQuery;

/**
 * Allows querying of {@link HistoricActivityInstanceQuery}s via native (SQL) queries
 * @author Bernd Ruecker (camunda)
 */
public interface NativeHistoricActivityInstanceQuery extends NativeQuery<NativeHistoricActivityInstanceQuery, HistoricActivityInstance> {

}
