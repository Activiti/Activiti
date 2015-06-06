package org.activiti5.engine.runtime;

import org.activiti5.engine.query.NativeQuery;

/**
 * Allows querying of {@link ProcessInstance}s via native (SQL) queries
 * @author Bernd Ruecker (camunda)
 */
public interface NativeProcessInstanceQuery extends NativeQuery<NativeProcessInstanceQuery, ProcessInstance> {

}
