package org.activiti.rest.api.cycle.session;

import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleServiceFactory;
import org.activiti.rest.api.cycle.session.CycleHttpSession.CycleRequestFilter;
import org.activiti.rest.util.ActivitiRequest;

/**
 * Filter for detecting connector-login requests
 * 
 * @author daniel.meyer@camunda.com
 */
public class ConnectorLoginRequestFilter implements CycleRequestFilter {

  public void beforeRequest(ActivitiRequest req) {
    Object connectorIdObject = req.getString("connector-login-request");
    if (connectorIdObject == null)
      return;

    String connectorId = (String) connectorIdObject;
    
    if(connectorId.length() == 0)
      return;
    
    // read credentials from request
    String username = req.getString(connectorId + "_username");
    String password = req.getString(connectorId + "_password");

    CycleRepositoryService repositoryService = CycleServiceFactory.getRepositoryService();
    // perform the login
    if(!repositoryService.login(username, password, connectorId)) {
      throw new RepositoryAuthenticationException("Cannot login to repository.", connectorId);
    }

  }

  public void afterRequest(ActivitiRequest req) {
    // do nothing
  }
  
}
