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

  public void interceptMethodCall(Method m, Object object, Object... args) {
    RepositoryConnector connector = (RepositoryConnector) object;
    if ("setConfiguration".equals(m.getName())) {
      return;
    }
    if ("getConfiguration".equals(m.getName())) {
      return;
    }
    if ("login".equals(m.getName())) {
      return;
    }
    if (!connector.isLoggedIn()) {
      throw new RepositoryAuthenticationException("Connector '" + connector.getConfiguration().getName() + "' is not logged in ", connector.getConfiguration()
              .getId());
    }

  }

}
