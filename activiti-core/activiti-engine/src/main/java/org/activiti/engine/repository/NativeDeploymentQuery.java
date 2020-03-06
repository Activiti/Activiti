package org.activiti.engine.repository;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti.engine.repository.Deployment}s via native (SQL) queries
 *
 */
@Internal
public interface NativeDeploymentQuery extends NativeQuery<NativeDeploymentQuery, Deployment> {

}