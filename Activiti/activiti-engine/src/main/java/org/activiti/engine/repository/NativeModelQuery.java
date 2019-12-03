package org.activiti.engine.repository;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti.engine.repository.Model}s via native (SQL) queries
 *
 */
@Internal
public interface NativeModelQuery extends NativeQuery<NativeModelQuery, Model> {

}