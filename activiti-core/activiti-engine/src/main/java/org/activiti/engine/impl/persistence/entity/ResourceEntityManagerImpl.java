/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.impl.persistence.entity;

import java.util.List;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.ResourceDataManager;

/**


 */
public class ResourceEntityManagerImpl extends AbstractEntityManager<ResourceEntity> implements ResourceEntityManager {

  protected ResourceDataManager resourceDataManager;

  public ResourceEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, ResourceDataManager resourceDataManager) {
    super(processEngineConfiguration);
    this.resourceDataManager = resourceDataManager;
  }

  @Override
  protected DataManager<ResourceEntity> getDataManager() {
    return resourceDataManager;
  }

  @Override
  public void deleteResourcesByDeploymentId(String deploymentId) {
    resourceDataManager.deleteResourcesByDeploymentId(deploymentId);
  }

  @Override
  public ResourceEntity findResourceByDeploymentIdAndResourceName(String deploymentId, String resourceName) {
    return resourceDataManager.findResourceByDeploymentIdAndResourceName(deploymentId, resourceName);
  }

  @Override
  public List<ResourceEntity> findResourcesByDeploymentId(String deploymentId) {
    return resourceDataManager.findResourcesByDeploymentId(deploymentId);
  }

  public ResourceDataManager getResourceDataManager() {
    return resourceDataManager;
  }

  public void setResourceDataManager(ResourceDataManager resourceDataManager) {
    this.resourceDataManager = resourceDataManager;
  }

}
