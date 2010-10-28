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
package org.activiti.engine.impl.repository;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;

/**
 * @author Tom Baeyens
 */
public class DeploymentBuilderImpl implements DeploymentBuilder {

  private static final long serialVersionUID = 1L;

  protected RepositoryServiceImpl repositoryService;
  protected DeploymentEntity deployment = new DeploymentEntity();
  protected boolean isDuplicateFilterEnabled = false;

  public DeploymentBuilderImpl(RepositoryServiceImpl repositoryService) {
    this.repositoryService = repositoryService;
  }

  public DeploymentBuilder addInputStream(String resourceName, InputStream inputStream) {
    if (inputStream==null) {
      throw new ActivitiException("inputStream for resource '"+resourceName+"' is null");
    }
    byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
    ResourceEntity resource = new ResourceEntity();
    resource.setName(resourceName);
    resource.setBytes(bytes);
    deployment.addResource(resource);
    return this;
  }

  public DeploymentBuilder addClasspathResource(String resource) {
    InputStream inputStream = ReflectUtil.getResourceAsStream(resource);
    if (inputStream==null) {
      throw new ActivitiException("resource '"+resource+"' not found");
    }
    return addInputStream(resource, inputStream);
  }

  public DeploymentBuilder addString(String resourceName, String text) {
    if (text==null) {
      throw new ActivitiException("text is null");
    }
    ResourceEntity resource = new ResourceEntity();
    resource.setName(resourceName);
    resource.setBytes(text.getBytes());
    deployment.addResource(resource);
    return this;
  }

  public DeploymentBuilder addZipInputStream(ZipInputStream zipInputStream) {
    try {
      ZipEntry entry = zipInputStream.getNextEntry();
      while (entry != null) {
        String entryName = entry.getName();
        byte[] bytes = IoUtil.readInputStream(zipInputStream, entryName);
        ResourceEntity resource = new ResourceEntity();
        resource.setName(entryName);
        resource.setBytes(bytes);
        deployment.addResource(resource);
        entry = zipInputStream.getNextEntry();
      }
    } catch (Exception e) {
      throw new ActivitiException("problem reading zip input stream", e);
    }
    return this;
  }

  public DeploymentBuilder name(String name) {
    deployment.setName(name);
    return this;
  }
  
  public DeploymentBuilder enableDuplicateFiltering() {
    isDuplicateFilterEnabled = true;
    return this;
  }

  public Deployment deploy() {
    return repositoryService.deploy(this);
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public DeploymentEntity getDeployment() {
    return deployment;
  }
  public boolean isDuplicateFilterEnabled() {
    return isDuplicateFilterEnabled;
  }
}
