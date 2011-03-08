package org.activiti.cycle.impl.connector;

import java.lang.reflect.Method;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.Interceptor;

/**
 * Interceptor for guaranteeing that Connectors are logged in.
 * 
 * @author daniel.meyer@camunda.com
 */
public class ConnectorLoginInterceptor implements Interceptor {

  private Logger log = Logger.getLogger(ConnectorLoginInterceptor.class.getName());

  // set of method-names we do not want to intercept calls to (check whether the
  // connector is loggedin)
  private static final SortedSet<String> ignoredMethods = new TreeSet<String>();

  static {
    ignoredMethods.add("getId");
    ignoredMethods.add("setId");
    ignoredMethods.add("getName");
    ignoredMethods.add("setName");
    ignoredMethods.add("startConfiguration");
    ignoredMethods.add("configurationFinished");
    ignoredMethods.add("addConfiguration");
    ignoredMethods.add("addConfigurationEntry");
    ignoredMethods.add("getUsername");
    ignoredMethods.add("getPassword");
    ignoredMethods.add("login");
    ignoredMethods.add("getConfigurationKeys");
  }

  public void beforeInvoke(Method m, Object object, Object... args) {
    RepositoryConnector connector = (RepositoryConnector) object;
    if (ignoredMethods.contains(m.getName())) {
      // let ignored methods pass
      return;
    }
    // for all other methods, check whether the connector is logged in.
    if (!connector.isLoggedIn()) {
      // the connector is not logged in, try to login:
      boolean loginSuccessful = false;
      Exception exception = null;
      try {
        PasswordEnabledRepositoryConnector pwEnabledRepositoryConnector = (PasswordEnabledRepositoryConnector) connector;
        String username = pwEnabledRepositoryConnector.getUsername();
        String password = pwEnabledRepositoryConnector.getPassword();
        loginSuccessful = pwEnabledRepositoryConnector.login(username, password);
      } catch (Exception e) {
        log.log(Level.WARNING, "Could not login connector '" + connector.getName() + "' with provided credentials.", e);
        // we cannot login with the provided credentials
        exception = e;
      }
      if (!loginSuccessful) {
        if (exception != null) {
          throw new RepositoryAuthenticationException("Connector '" + connector.getName() + "' is not logged in ", connector.getId(), exception);
        }
        throw new RepositoryAuthenticationException("Connector '" + connector.getName() + "' is not logged in ", connector.getId());
      }
    }
  }

  public void afterInvoke(Method m, Object object, Object invocationResult, Object... args) {
    // do nothing
  }

}
