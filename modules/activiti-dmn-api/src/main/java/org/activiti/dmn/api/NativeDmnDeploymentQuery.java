package org.activiti.dmn.api;

/**
 * Allows querying of {@link org.activiti.engine.repository.DmnDeployment}s via native (SQL) queries
 * 
 * @author Tijs Rademakers
 */
public interface NativeDmnDeploymentQuery extends NativeQuery<NativeDmnDeploymentQuery, DmnDeployment> {

}