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

import org.activiti.engine.impl.persistence.RepositorySession;
import org.activiti.engine.impl.persistence.process.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.repository.DeploymentEntity;
import org.activiti.engine.impl.persistence.repository.ResourceEntity;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.repository.Deployer;
import org.activiti.impl.tx.Session;


/**
 * @author Tom Baeyens
 */
public class DbRepositorySession implements Session, RepositorySession {

  protected List<Deployer> deployers = new ArrayList<Deployer>();
  protected Map<String, ProcessDefinitionEntity> processDefinitionCache = new HashMap<String, ProcessDefinitionEntity>(); 
  protected DbSqlSession dbSqlSession;
  
  public DbRepositorySession(List<Deployer> deployers, Map<String, ProcessDefinitionEntity> processDefinitionCache) {
    this.deployers = deployers;
    this.processDefinitionCache = processDefinitionCache;
    this.dbSqlSession = CommandContext
      .getCurrentCommandContext()
      .getSession(DbSqlSession.class);
  }

  public void close() {
  }

  public void flush() {
  }

  public void insertDeployment(DeploymentEntity deployment) {
    dbSqlSession.insert(deployment);
  }

  public void deleteDeployment(DeploymentEntity deployment) {
    dbSqlSession.delete(deployment);
  }

  public void deleteResource(ResourceEntity resource) {
    dbSqlSession.delete(resource);
  }

  public void insertResource(ResourceEntity resource) {
    dbSqlSession.insert(resource);
  }
}
