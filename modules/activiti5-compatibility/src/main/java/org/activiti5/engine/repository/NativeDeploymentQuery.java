package org.activiti5.engine.repository;

import org.activiti5.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti5.engine.repository.Deployment}s via native (SQL) queries
 * @author Henry Yan(http://www.kafeitu.me)
 */
public interface NativeDeploymentQuery extends NativeQuery<NativeDeploymentQuery, Deployment> {

}