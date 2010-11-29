package org.activiti.cycle.impl.conf;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.service.CycleService;

/**
 * Helper-class for handling {@link RepositoryConnectorConfiguration}s in a
 * generic way. Capabilities:
 * <ul>
 * <li>extracting available fields from a configuration class
 * {@link #getConfigurationFields(String)}</li>
 * <li>extracting the current values for a configuration instance
 * {@link #getValueMap(RepositoryConnectorConfiguration)}</li>
 * <li>setting new values for a given configuration instance
 * {@link #setConfigurationfields(Map, Object)}</li>
 * </ul>
 * 
 * @author daniel.meyer@camunda.com
 */
public class RepositoryConfigurationHandler {

  static Logger log = Logger.getLogger(RepositoryConfigurationHandler.class.getCanonicalName());

  /**
   * Sets the values of 'fields' on 'repositoryConnectorInstance'.
   * 
   * @param fields
   *          a map of configuration values where the keys are field-names and
   *          the values are the configuration values.
   * @param repositoryConnectorInstance
   *          the {@link RepositoryConnectorConfiguration} instance to set the
   *          values on.
   */
  public static void setConfigurationfields(Map<String, String> fields, Object repositoryConnectorInstance) {
    try {
      for (String fieldName : fields.keySet()) {
        String methodname = "set";
        methodname += fieldName.substring(0, 1).toUpperCase();
        methodname += fieldName.substring(1);
        // TODO: at the moment we only support strings
        @SuppressWarnings("rawtypes")
        Class clazz = String.class;
        Method method = repositoryConnectorInstance.getClass().getMethod(methodname, clazz);
        method.invoke(repositoryConnectorInstance, fields.get(fieldName));
      }
    } catch (Exception e) {
      log.log(Level.WARNING, "could not set fieldvalues " + e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * @see CycleService#getConfigurationFields(String)
   */
  public static Map<String, String> getConfigurationFields(String configurationClazzName) {
    Map<String, String> result = new HashMap<String, String>();

    try {
      Class< ? extends RepositoryConnectorConfiguration> configuration = (Class< ? extends RepositoryConnectorConfiguration>) Class
              .forName(configurationClazzName);

      addFields(result, configuration);

    } catch (ClassNotFoundException e) {
      log.log(Level.WARNING, "could not get configuration fields " + e.getMessage(), e);
      throw new RuntimeException(e);
    }
    return result;
  }

  private static void addFields(Map<String, String> result, Class< ? extends RepositoryConnectorConfiguration> configuration) {

    for (Method method : configuration.getMethods()) {
      // todo find a better way to detect setters, maybe annotations?
      if (!method.getName().startsWith("set") || method.getParameterTypes().length != 1
              || !method.getParameterTypes()[0].getCanonicalName().equals(String.class.getCanonicalName())) {
        continue;
      }
      String fieldName = method.getName();
      fieldName = fieldName.replace("set", "");
      fieldName = fieldName.substring(0, 1).toLowerCase().concat(fieldName.substring(1));
      result.put(fieldName, String.class.getCanonicalName());
    }
  }

  /**
   * @see CycleService#getRepositoryConnectorConfiguration(String, String)
   */
  public static Map<String, String> getValueMap(RepositoryConnectorConfiguration repoConfiguration) {
    try {
      Map<String, String> result = new HashMap<String, String>();
      Map<String, String> fieldNames = new HashMap<String, String>();
      addFields(fieldNames, repoConfiguration.getClass());

      for (String fieldName : fieldNames.keySet()) {
        String methodname = "get";
        methodname += fieldName.substring(0, 1).toUpperCase();
        methodname += fieldName.substring(1);
        Method getter = repoConfiguration.getClass().getMethod(methodname);
        Object value = getter.invoke(repoConfiguration);
        if (value != null) {
          result.put(fieldName, value.toString());
        } else {
          result.put(fieldName, "");
        }
      }

      return result;
    } catch (Exception e) {
      log.log(Level.WARNING, "could not set fieldvalues " + e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
