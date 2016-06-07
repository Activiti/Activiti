package org.activiti.dmn.engine.repository;

import org.activiti.dmn.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti.engine.repository.DmnDeployment}s via native (SQL) queries
 * 
 * @author Tijs Rademakers
 */
public interface NativeDmnDeploymentQuery extends NativeQuery<NativeDmnDeploymentQuery, DmnDeployment> {

}