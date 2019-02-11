package org.activiti.engine.history;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link HistoricTaskInstanceQuery}s via native (SQL) queries
 *
 */
@Internal
public interface NativeHistoricTaskInstanceQuery extends NativeQuery<NativeHistoricTaskInstanceQuery, HistoricTaskInstance> {

}
