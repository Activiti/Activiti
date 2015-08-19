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

package org.activiti.compatibility.wrapper;

import org.activiti.engine.repository.ProcessDefinition;

/**
 * Wraps an Activiti 5 process definition to an Activiti 6 {@link ProcessDefinition}.
 * 
 * @author Tijs Rademakers
 */
public class Activiti5ProcessDefinitionWrapper implements ProcessDefinition {

  private org.activiti5.engine.repository.ProcessDefinition activit5ProcessDefinition;
  
  public Activiti5ProcessDefinitionWrapper(org.activiti5.engine.repository.ProcessDefinition activit5ProcessDefinition) {
    this.activit5ProcessDefinition = activit5ProcessDefinition;
  }

  @Override
  public String getId() {
    return activit5ProcessDefinition.getId();
  }

  @Override
  public String getCategory() {
    return activit5ProcessDefinition.getCategory();
  }

  @Override
  public String getName() {
    return activit5ProcessDefinition.getName();
  }

  @Override
  public String getKey() {
    return activit5ProcessDefinition.getKey();
  }

  @Override
  public String getDescription() {
    return activit5ProcessDefinition.getDescription();
  }

  @Override
  public int getVersion() {
    return activit5ProcessDefinition.getVersion();
  }

  @Override
  public String getResourceName() {
    return activit5ProcessDefinition.getResourceName();
  }

  @Override
  public String getDeploymentId() {
    return activit5ProcessDefinition.getDeploymentId();
  }

  @Override
  public String getDiagramResourceName() {
    return activit5ProcessDefinition.getDiagramResourceName();
  }

  @Override
  public boolean hasStartFormKey() {
    return activit5ProcessDefinition.hasStartFormKey();
  }

  @Override
  public boolean hasGraphicalNotation() {
    return activit5ProcessDefinition.hasGraphicalNotation();
  }

  @Override
  public boolean isSuspended() {
    return activit5ProcessDefinition.isSuspended();
  }

  @Override
  public String getTenantId() {
    return activit5ProcessDefinition.getTenantId();
  }
  
  public org.activiti5.engine.repository.ProcessDefinition getRawObject() {
    return activit5ProcessDefinition;
  }

}
