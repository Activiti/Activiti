package org.activiti.engine.history;

import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti.engine.history.HistoricDetail}s via native (SQL) queries
 * @author Henry Yan(http://www.kafeitu.me)
 */
public interface NativeHistoricDetailQuery extends NativeQuery<NativeHistoricDetailQuery, HistoricDetail> {

}