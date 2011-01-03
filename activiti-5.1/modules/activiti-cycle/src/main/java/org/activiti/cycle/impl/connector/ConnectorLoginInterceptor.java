package org.activiti.cycle.impl.connector;

import java.lang.reflect.Method;

import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.Interceptor;

/**
 * Interceptor for guaranteeing that Connectors are logged in.
 * 
 * @author daniel.meyer@camunda.com
 */
public class ConnectorLoginInterceptor implements Interceptor {

  public void beforeInvoke(Method m, Object object, Object... args) {
    RepositoryConnector connector = (RepositoryConnector) object;
    if ("setConfiguration".equals(m.getName())) {
      // let the invocation of the "setConfiguration"-method pass
      return;
    }
    if ("getConfiguration".equals(m.getName())) {
      // let the invocation of the "getConfiguration"-method pass
      return;
    }
    if ("login".equals(m.getName())) {
      // let the invocation of the "login"-method pass
      return;
    }
    // for all other methods, check whether the connector is logged in.
    if (!connector.isLoggedIn()) {
      // the connector is not logged in, block invocation
      throw new RepositoryAuthenticationException("Connector '" + connector.getConfiguration().getName() + "' is not logged in ", connector.getConfiguration()
              .getId());
    }

  }

  public void afterInvoke(Method m, Object object, Object invocationResult, Object... args) {
    // do nothing
  }

}
