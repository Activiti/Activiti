package org.activiti.form.engine.repository;

import org.activiti.form.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti.FormDeployment.repository.DmnDeployment}s via native (SQL) queries
 * 
 * @author Tijs Rademakers
 */
public interface NativeFormDeploymentQuery extends NativeQuery<NativeFormDeploymentQuery, FormDeployment> {

}