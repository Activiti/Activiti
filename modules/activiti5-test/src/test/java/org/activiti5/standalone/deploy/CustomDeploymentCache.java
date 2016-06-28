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
package org.activiti5.standalone.deploy;

import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;

/**
 * Very simplistic cache implementation that only caches one process definition.
 * 
 * @author Joram Barrez
 */
public class CustomDeploymentCache implements DeploymentCache<ProcessDefinitionCacheEntry> {
  
  protected String id;
  
  protected ProcessDefinitionCacheEntry processDefinition;
  
  @Override
  public ProcessDefinitionCacheEntry get(String id) {
    if (id.equals(id)) {
      return processDefinition;
    }
    return null;
  }

  @Override
  public void add(String id, ProcessDefinitionCacheEntry object) {
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
  public ProcessDefinitionCacheEntry getCachedProcessDefinition() {
    return processDefinition;
  }

  @Override
  public boolean contains(String id) {
    if (id.equals(id)) {
      return true;
    } else {
      return false;
    }
  }

}
