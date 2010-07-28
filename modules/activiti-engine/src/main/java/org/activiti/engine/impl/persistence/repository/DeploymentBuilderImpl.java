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
package org.activiti.engine.impl.persistence.repository;

import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.Deployment;
import org.activiti.engine.DeploymentBuilder;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.impl.util.IoUtil;

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
    deployment.setResources(new HashMap<String, ResourceEntity>());
  }

  public DeploymentBuilder addInputStream(String resourceName, InputStream inputStream) {
    byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
    ResourceEntity resource = new ResourceEntity();
    resource.setName(resourceName);
    resource.setBytes(bytes);
    deployment.getResources().put(resourceName, resource);
    return this;
  }

  public DeploymentBuilder addClasspathResource(String resource) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(resource);
    return addInputStream(resource, inputStream);
  }

  public DeploymentBuilder addString(String resourceName, String text) {
    ResourceEntity resource = new ResourceEntity();
    resource.setName(resourceName);
    resource.setBytes(text.getBytes());
    deployment.getResources().put(resourceName, resource);
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
        deployment.getResources().put(entryName, resource);
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
