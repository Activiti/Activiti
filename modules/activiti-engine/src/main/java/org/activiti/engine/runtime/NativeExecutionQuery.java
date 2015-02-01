package org.activiti.engine.runtime;

import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link Execution}s via native (SQL) queries
 * @author Bernd Ruecker (camunda)
 */
public interface NativeExecutionQuery extends NativeQuery<NativeExecutionQuery, Execution> {

}
