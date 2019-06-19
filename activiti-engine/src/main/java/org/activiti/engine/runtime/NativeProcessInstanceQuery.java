package org.activiti.engine.runtime;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link ProcessInstance}s via native (SQL) queries
 * 

 */
@Internal
public interface NativeProcessInstanceQuery extends NativeQuery<NativeProcessInstanceQuery, ProcessInstance> {

}
