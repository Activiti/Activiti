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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.repository.Deployer;
import org.activiti.engine.impl.persistence.repository.ProcessDefinitionEntity;
import org.activiti.impl.interceptor.SessionFactory;
import org.activiti.impl.tx.Session;


/**
 * @author Tom Baeyens
 */
public class DbRepositorySessionFactory implements SessionFactory {
  
  protected List<Deployer> deployers = new ArrayList<Deployer>();
  protected Map<String, ProcessDefinitionEntity> processDefinitionCache = new HashMap<String, ProcessDefinitionEntity>(); 

  public Session openSession() {
    return new DbRepositorySession(this);
  }

  
  public List<Deployer> getDeployers() {
    return deployers;
  }

  
  public void setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
  }

  
  public Map<String, ProcessDefinitionEntity> getProcessDefinitionCache() {
    return processDefinitionCache;
  }

  
  public void setProcessDefinitionCache(Map<String, ProcessDefinitionEntity> processDefinitionCache) {
    this.processDefinitionCache = processDefinitionCache;
  }
}
