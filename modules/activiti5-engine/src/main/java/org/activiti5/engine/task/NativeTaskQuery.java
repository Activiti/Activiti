package org.activiti5.engine.task;

import org.activiti5.engine.query.NativeQuery;

/**
 * Allows querying of {@link Task}s via native (SQL) queries
 * @author Bernd Ruecker (camunda)
 */
public interface NativeTaskQuery extends NativeQuery<NativeTaskQuery, Task> {

}
