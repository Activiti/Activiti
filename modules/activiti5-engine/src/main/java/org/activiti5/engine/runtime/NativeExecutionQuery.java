package org.activiti5.engine.runtime;

import org.activiti5.engine.query.NativeQuery;

/**
 * Allows querying of {@link Execution}s via native (SQL) queries
 * @author Bernd Ruecker (camunda)
 */
public interface NativeExecutionQuery extends NativeQuery<NativeExecutionQuery, Execution> {

}
