package org.activiti.rest.api.cycle.session;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.PasswordEnabledRepositoryConnector;
import org.activiti.cycle.service.CycleServiceFactory;
import org.activiti.rest.util.ActivitiRequest;

/**
 * Initializes the Cycle Http-Session. 
 * 
 * TODO: find a better place for this, a ServletFilter?
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleHttpSession {

  public static interface CycleRequestFilter {

    public void doFilter(ActivitiRequest req);
  }

  public static Set<CycleRequestFilter> requestFilters = new HashSet<CycleRequestFilter>();

  static {
    requestFilters.add(new ConnectorLoginRequestFilter());
  }

  public static void openSession(ActivitiRequest req) {
    HttpSession httpSession = req.getHttpServletRequest().getSession(true);
    String cuid = req.getCurrentUserId();

    // TODO: find a better place for this ?
    CycleComponentFactory.registerServletContext(httpSession.getServletContext());

    // Makes the HttpSession available as CycleSessionContext
    CycleSessionContext.setContext(new HttpSessionContext(httpSession));
    // make the current user id available in the session context
    CycleSessionContext.set("cuid", cuid);

    // invoke request filters
    for (CycleRequestFilter requestFilter : requestFilters) {
      requestFilter.doFilter(req);
    }
  }

  public static void tryConnectorLogin(ActivitiRequest req, String connectorId) {
    RepositoryConnector connector = null;
    // locate connector-instance:
    RuntimeConnectorList connectorList = CycleSessionContext.get(RuntimeConnectorList.class);
    connector = connectorList.getConnectorById(connectorId);
    if (connector == null) {
      throw new RuntimeException("Cannot login to repository with id '" + connectorId + "', no such repository.");
    }

    String username = null;
    String password = null;

    // try to read credentials from configuration
    if (connector instanceof PasswordEnabledRepositoryConnector) {     
      username = ((PasswordEnabledRepositoryConnector) connector).getUsername();
      password = ((PasswordEnabledRepositoryConnector) connector).getPassword();
    }

    // TODO : get from cookie

    // try to login:
    try {
      CycleServiceFactory.getRepositoryService().login(username, password, connectorId);
    } catch (RepositoryException ex) {
      // wrap in Authentication exception:
      throw new RepositoryAuthenticationException("Cannot login to repository '" + connectorId + "'.", connectorId, ex);
    }
  }

  public static void closeSession() {
    CycleSessionContext.clearContext();
  }

}
