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

package org.activiti.engine.impl.persistence.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationAware;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.repository.Deployer;
import org.activiti.engine.impl.persistence.repository.ProcessDefinitionEntity;


/**
 * @author Tom Baeyens
 */
public class DbRepositorySessionFactory implements SessionFactory, ProcessEngineConfigurationAware {
  
  protected Map<String, ProcessDefinitionEntity> processDefinitionCache = new HashMap<String, ProcessDefinitionEntity>(); 
  protected List<Deployer> deployers;
  protected long idBlockSize;

  public void configurationCompleted(ProcessEngineConfiguration processEngineConfiguration) {
    deployers = processEngineConfiguration.getDeployers();
    idBlockSize = processEngineConfiguration.getIdBlockSize();
  }

  public Session openSession() {
    return new DbRepositorySession(this);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public Map<String, ProcessDefinitionEntity> getProcessDefinitionCache() {
    return processDefinitionCache;
  }
  
  public void setProcessDefinitionCache(Map<String, ProcessDefinitionEntity> processDefinitionCache) {
    this.processDefinitionCache = processDefinitionCache;
  }
  
  public List<Deployer> getDeployers() {
    return deployers;
  }
  
  public void setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
  }
  
  public long getIdBlockSize() {
    return idBlockSize;
  }

  public void setIdBlockSize(long idBlockSize) {
    this.idBlockSize = idBlockSize;
  }
}
