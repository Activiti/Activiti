/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.cycle.service;

import java.util.List;
import java.util.Map;

import org.activiti.cycle.RepositoryConnectorConfiguration;

/**
 * Cycle service used for manipulating the configuration settings. ATM,
 * manipulation of RepositoryConnectorConfiguraitons is supported, using a
 * String-based interface.
 * <p />
 * Retrieve an instance of this Service by
 * {@link CycleServiceFactory#getConfigurationService()}
 * 
 * @see CycleService
 * @author daniel.meyer@camunda.com
 */
public interface CycleConfigurationService {

  /**
   * Deletes the {@link RepositoryConnectorConfiguration} designated by
   * 'connectorConfigurationId'.
   * 
   * @param connectorConfigurationId
   *          the of the connector we want to delete, as returned by
   *          {@link RepositoryConnectorConfiguration#getId()}.
   */
  public void deleteRepositoryConnectorConfiguration(String connectorConfigurationId);

  /**
   * Returns all available {@link RepositoryConnectorConfiguration}
   * implementations.
   * 
   * @return a map such that each key is a canonical classname of a
   *         {@link RepositoryConnectorConfiguration} and the corresponding
   *         value is a human-readable name for this connector-type.
   */
  public Map<String, String> getAvailableRepositoryConnectorConfiguatationClasses();

  /**
   * Returns a map of available configuration fields for the given class.
   * <p>
   * TODO: at the moment we recognize a field if we find a setter-method which
   * takes a single parameter of type String. A better solution would be IMO to
   * use annotations and annotate setters with sth. like
   * "@ConnectorConfigurationField".
   * 
   * @param configurationClazzName
   *          . the name of the {@link RepositoryConnectorConfiguration}
   * @return a map of field-type mappings.
   * 
   */
  public Map<String, String> getConfigurationFields(String configurationClazzName);

  /**
   * Returns a map of {@link RepositoryConnectorConfiguration}s for the provided
   * user.
   * 
   * @return a map of lists, such that each key represents the canonical
   *         classname of a {@link RepositoryConnectorConfiguration}
   *         implementation and the corresponding value is a list of Ids, as
   *         returned by {@link RepositoryConnectorConfiguration#getId()}.
   */
  public Map<String, List<String>> getConfiguredRepositoryConnectors();

  /**
   * Returns the {@link ConfigurationContainer} for the current user.
   */
  public List<RepositoryConnectorConfiguration> getConnectorConfigurations();

  /**
   * Returns a field-value map of the current configuration settings represented
   * by 'repoConfiguration'.
   * <p />
   * Note: this method returns values for the fields returned by
   * {@link #getConfigurationFields(String)}
   * 
   * @param repoConfiguration
   *          the {@link RepositoryConnectorConfiguration} instance to extract
   *          the values from.
   * @return a map of field-name / value pairs.
   */
  public Map<String, String> getRepositoryConnectorConfiguration(String connectorConfigurationId);

  /**
   * Creates / Updates the {@link RepositoryConnectorConfiguration} designated
   * by the provided 'configurationId' and owned by the provided
   * 'currentUserId'. If no such configuration exists, a new configuration is
   * created, as an instance of the provided 'configurationClass'.
   * 
   * @param configurationClass
   *          the canonical classname of the
   *          {@link RepositoryConnectorConfiguration} implementation we want to
   *          create / update.
   * @param configurationId
   *          the id as returned by
   *          {@link RepositoryConnectorConfiguration#getId()}.
   * @param values
   *          the configuration values for this configuration as am map of
   *          fieldname / value pairs.
   */
  public void updateRepositoryConnectorConfiguration(String configurationClass, String configurationId, Map<String, String> values);

  /**
   * Retrieve a global configuration value
   */
  public String getConfigurationValue(String groupId, String key);

  /**
   * set a global configuration value
   */
  public void setConfigurationValue(String groupId, String key, String value);

  /**
   * get a global configuration value, providing a default value
   */
  public String getConfigurationValue(String groupId, String key, String defaultValue);

  /**
   * @return an array of configuration groups.
   */
  public String[] getConfigurationGroups();

  /**
   * @param groupId
   *          the groupId to retrieve the configuration values for.
   * @return an map of configuration Key/Value pairs for the provided groupid
   */
  public Map<String, String> getConfigurationValuesForGroup(String groupId);

}
