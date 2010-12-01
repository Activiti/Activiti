package org.activiti.rest.api.cycle.session;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.CycleSessionContext;
import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.view.TagConnectorConfiguration;
import org.activiti.cycle.impl.plugin.PluginFinder;
import org.activiti.cycle.service.CycleServiceFactory;
import org.activiti.cycle.service.CycleRepositoryService.RuntimeConnectorList;
import org.activiti.rest.util.ActivitiRequest;

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
    PluginFinder.registerServletContext(httpSession.getServletContext());

    // Makes the HttpSession available as CycleSessionContext
    CycleSessionContext.setCurrentContext(new HttpSessionContext(httpSession));
    CycleSessionContext.setInCurrentContext("cuid", cuid);

    // load list of runtime connectors
    // try to retrieve list of connectors form the session
    RuntimeConnectorList connectorList = CycleSessionContext.getFromCurrentContext(RuntimeConnectorList.class);
    if (connectorList == null) {
      // store new connector-list in session
      connectorList = new RuntimeConnectorList();
      CycleSessionContext.setInCurrentContext(RuntimeConnectorList.class, connectorList);
    }
    if (connectorList.connectors == null) {
      // populate list of connectors if empty
      ConfigurationContainer container = CycleServiceFactory.getConfigurationService().getConfigurationContainer();
      List<RepositoryConnector> connectors = container.getConnectorList();
      // add tag connector hard coded for the moment (at the first node in the
      // tree)
      connectors.add(0, new TagConnectorConfiguration().createConnector());
      connectorList.connectors = connectors;
    }
    
    // invoke request filters
    for (CycleRequestFilter requestFilter : requestFilters) {
      requestFilter.doFilter(req);
    }
  }

  public static void tryConnectorLogin(ActivitiRequest req, String connectorId) {
    RepositoryConnector connector = null;
    // locate connector-instance:
    RuntimeConnectorList connectorList = CycleSessionContext.getFromCurrentContext(RuntimeConnectorList.class);
    connector = connectorList.getConnectorById(connectorId);
    if (connector == null) {
      throw new RuntimeException("Cannot login to repository with id '" + connectorId + "', no such repository.");
    }

    String username = null;
    String password = null;

    // try to read credentials from configuration
    if (connector.getConfiguration() instanceof PasswordEnabledRepositoryConnectorConfiguration) {
      PasswordEnabledRepositoryConnectorConfiguration passwordEnabledRepositoryConnectorConfiguration = (PasswordEnabledRepositoryConnectorConfiguration) connector
              .getConfiguration();
      username = passwordEnabledRepositoryConnectorConfiguration.getUser();
      password = passwordEnabledRepositoryConnectorConfiguration.getPassword();
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
    CycleSessionContext.clearCurrentContext();
  }

}
