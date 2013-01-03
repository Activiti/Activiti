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
package org.activiti.standalone.deploy;

import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * Very simplistic cache implementation that only caches one process definition.
 * 
 * @author Joram Barrez
 */
public class CustomDeploymentCache implements DeploymentCache<ProcessDefinitionEntity> {
  
  protected String id;
  
  protected ProcessDefinitionEntity processDefinition;
  
  @Override
  public ProcessDefinitionEntity get(String id) {
    if (id.equals(id)) {
      return processDefinition;
    }
    return null;
  }

  @Override
  public void add(String id, ProcessDefinitionEntity object) {
    this.id = id;
    this.processDefinition = object;
  }

  @Override
  public void remove(String id) {
    if (id.equals(id)) {
      this.id = null;
      this.processDefinition = null;
    }
  }

  @Override
  public void clear() {
    this.id = null;
    this.processDefinition = null;
  }
  
  // For testing purposes only
  public ProcessDefinitionEntity getCachedProcessDefinition() {
    return processDefinition;
  }

}
