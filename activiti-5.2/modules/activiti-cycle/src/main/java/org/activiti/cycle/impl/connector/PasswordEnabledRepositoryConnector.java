package org.activiti.cycle.impl.connector;

import org.activiti.cycle.RepositoryConnector;

/**
 * Interface implemented by {@link RepositoryConnector}s which need a password
 * an a username.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface PasswordEnabledRepositoryConnector extends RepositoryConnector {

  public String getPassword();

  public String getUsername();

}
