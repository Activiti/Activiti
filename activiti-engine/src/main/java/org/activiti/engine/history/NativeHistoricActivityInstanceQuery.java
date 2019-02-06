package org.activiti.engine.history;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link HistoricActivityInstanceQuery}s via native (SQL) queries
 *
 */
@Internal
public interface NativeHistoricActivityInstanceQuery extends NativeQuery<NativeHistoricActivityInstanceQuery, HistoricActivityInstance> {

}
