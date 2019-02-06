package org.activiti.engine.runtime;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link Execution}s via native (SQL) queries
 * 

 */
@Internal
public interface NativeExecutionQuery extends NativeQuery<NativeExecutionQuery, Execution> {

}
