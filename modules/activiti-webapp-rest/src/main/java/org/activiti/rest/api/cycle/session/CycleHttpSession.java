package org.activiti.rest.api.cycle.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.CycleSessionContext;
import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.view.TagConnectorConfiguration;
import org.activiti.cycle.impl.plugin.PluginFinder;
import org.activiti.cycle.impl.service.CycleServiceImpl;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleRepositoryService.RuntimeConnectorList;
import org.activiti.rest.util.ActivitiRequest;

public class CycleHttpSession {

  public static void openSession(ActivitiRequest req) {
    HttpSession httpSession = req.getHttpServletRequest().getSession(true);
    String cuid = req.getCurrentUserId();

    // TODO: find a better place for this ?
    PluginFinder.registerServletContext(httpSession.getServletContext());

    // Makes the HttpSession available as CycleSessionContext
    CycleSessionContext.setCurrentContext(new HttpSessionContext(httpSession));
    CycleSessionContext.setInCurrentContext("cuid", cuid);

    boolean couldRestoreConnectors = loadRuntimeRepositoryConnectors();
    checkPasswordEnabledConnectors(req);
    if (!couldRestoreConnectors) {
      performLogin();
    }
  }

  private static boolean loadRuntimeRepositoryConnectors() {
    boolean couldRestore = false;
    // try to retrieve list of connectors form the session
    RuntimeConnectorList connectorList = CycleSessionContext.getFromCurrentContext(RuntimeConnectorList.class);
    if (connectorList == null) {
      // store new connector-list in session
      connectorList = new RuntimeConnectorList();
      CycleSessionContext.setInCurrentContext(RuntimeConnectorList.class, connectorList);
    }
    if (connectorList.connectors == null) {
      // populate list of connectors if empty
      ConfigurationContainer container = CycleServiceImpl.getInstance().getConfigurationService().getConfigurationContainer();
      List<RepositoryConnector> connectors = container.getConnectorList();
      connectorList.connectors = connectors;
    } else {
      // connectors could be restored
      couldRestore = true;
    }
    return couldRestore;
  }

  private static void checkPasswordEnabledConnectors(ActivitiRequest req) {

    // this is guaranteed to be initialized now
    RuntimeConnectorList connectorList = CycleSessionContext.getFromCurrentContext(RuntimeConnectorList.class);

    // Make sure we know username and password for all connectors that require
    // login. If it is not stored in the users configuration it should be
    // provided as a parameter in the request.
    Map<String, String> connectorsWithoutLoginMap = new HashMap<String, String>();
    for (RepositoryConnector connector : getPasswordEnabledConnectors(connectorList.connectors)) {
      PasswordEnabledRepositoryConnectorConfiguration conf = (PasswordEnabledRepositoryConnectorConfiguration) connector.getConfiguration();
      String username = req.getString(conf.getId() + "_username");
      String password = req.getString(conf.getId() + "_password");

      if (username != null && password != null) {
        // Remove the connector from the configuration for this session since
        // the user pressed cancel in the authentication dialog.
        if (username.equals("\"\"") && password.equals("\"\"")) {
          connectorList.connectors.remove(connector);
        } else {
          conf.setUser(username);
          conf.setPassword(password);
        }
      } else if (conf.getUser() == null || conf.getPassword() == null) {
        connectorsWithoutLoginMap.put(conf.getId(), conf.getName());
      }
      // If one or more logins are missing (not provided in either the
      // configuration or as HTTP parameter) we'll throw an authentication
      // exception with the list of connectors that are missing login
      // information
    }
    if (connectorsWithoutLoginMap.size() > 0) {
      // TODO: i18n
      throw new RepositoryAuthenticationException("Please provide your username and password for the following repositories:", connectorsWithoutLoginMap);
    }
  }

  private static void performLogin() {

    // this is guaranteed to be initialized now
    RuntimeConnectorList connectorList = CycleSessionContext.getFromCurrentContext(RuntimeConnectorList.class);

    CycleRepositoryService repositoryService = CycleServiceImpl.getInstance().getRepositoryService();

    // If we get here we can assume that all the required logins are
    // available
    // and we can now perform the login for those connectors that require it
    for (RepositoryConnector connector : connectorList.connectors) {
      if (PasswordEnabledRepositoryConnectorConfiguration.class.isInstance(connector.getConfiguration())) {
        PasswordEnabledRepositoryConnectorConfiguration conf = (PasswordEnabledRepositoryConnectorConfiguration) connector.getConfiguration();
        String username = conf.getUser();
        String password = conf.getPassword();
        try {
          repositoryService.login(username, password, conf.getId());
        } catch (RepositoryException e) {
          Map<String, String> connectorMap = new HashMap<String, String>();
          connectorMap.put(conf.getId(), conf.getName());
          throw new RepositoryAuthenticationException("Repository authentication error: couldn't login to " + conf.getName(), connectorMap, e);
        }
      }
    }

    // add tag connector hard coded for the moment (at the first node in the
    // tree)
    // TODO: move to better place !!!
    connectorList.connectors.add(0, new TagConnectorConfiguration(CycleServiceImpl.getInstance()).createConnector());

  }

  private static List<RepositoryConnector> getPasswordEnabledConnectors(List<RepositoryConnector> connectors) {
    List<RepositoryConnector> loginEnabledconnectors = new ArrayList<RepositoryConnector>();
    for (RepositoryConnector connector : connectors) {
      if (connector.getConfiguration() instanceof PasswordEnabledRepositoryConnectorConfiguration) {
        loginEnabledconnectors.add(connector);
      }
    }
    return loginEnabledconnectors;
  }

  public static void closeSession() {
    CycleSessionContext.clearCurrentContext();
  }

}
