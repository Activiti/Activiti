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
package org.activiti.cycle;

import java.util.List;

import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;


/**
 * This interface provides the methods to read, write and manage configuration
 * entities.
 * 
 * @author christian.lipphardt@camunda.com
 */
public interface CycleService {
  
  // TODO: Add this here?
  // public RepositoryConnector createRepositoryConnector(String
  // configuationName);

  public ConfigurationContainer getConfiguration(String name);
  
  public void saveConfiguration(ConfigurationContainer container);

  

  // TODO: DIscuss: I wouldn't mention the repository connectors here at all,
  // or?
  // public void registerRepositoryConnector(Class< ? extends
  // RepositoryConnector> test);
  //
  // public List<Class< ? extends RepositoryConnector>>
  // getRegisteredRepositoryConnectors();

  // start crud methods

  // public RepositoryConnectorConfiguration
  // createRepositoryConfiguration(Class< ? extends RepositoryConnector>
  // repositoryConnector, String user,
  // String password, String basePath);
//
//  public void persistRepositoryConfiguration(RepositoryConnectorConfiguration config);
//
//  // Why this? the persist should persist it immediately?
//  // Breaks CRUD/DAO, or not?
//  // public void persistAllRepositoryConfigurations();
//
//  public List<RepositoryConnectorConfiguration> findAllRepositoryConfigurations();
//
//  public void removeRepositoryConfiguration(String name);

//  public RepositoryConnectorConfiguration getRepositoryConfiguration(String name);

  // end crud methods

  // public List<RepositoryConnector>
  // createRepositoryConnectorsFromConfigurations();

  // public RepositoryConnector
  // createRepositoryConnectorFromConfiguration(RepositoryConnectorConfiguration
  // repositoryConfig);
  
  //----- start method declaration for cycle persistence -----
//  public ProcessEngineConfiguration getProcessEngineConfiguration();
//  
//  public void createAndInsert(String configXML, String id);
//  
//  public CycleConfigEntity selectById(String id);
//  
//  public void deleteById(String id);
//  
//  public void updateById(CycleConfigEntity cycleConfig);
  
}
