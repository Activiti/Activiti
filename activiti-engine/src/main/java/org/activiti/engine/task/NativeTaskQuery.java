package org.activiti.engine.task;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link Task}s via native (SQL) queries
 *
 */
@Internal
public interface NativeTaskQuery extends NativeQuery<NativeTaskQuery, Task> {

}
