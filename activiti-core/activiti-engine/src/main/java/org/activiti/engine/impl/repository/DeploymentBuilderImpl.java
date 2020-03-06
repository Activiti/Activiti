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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.core.common.project.model.ProjectManifest;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.core.io.Resource;

/**


 */
public class DeploymentBuilderImpl implements DeploymentBuilder, Serializable {

  private static final long serialVersionUID = 1L;
  protected static final String DEFAULT_ENCODING = "UTF-8";

  protected transient RepositoryServiceImpl repositoryService;
  protected transient ResourceEntityManager resourceEntityManager;
  
  protected DeploymentEntity deployment;
  protected boolean isBpmn20XsdValidationEnabled = true;
  protected boolean isProcessValidationEnabled = true;
  protected boolean isDuplicateFilterEnabled;
  protected Date processDefinitionsActivationDate;
  protected Map<String, Object> deploymentProperties = new HashMap<>();
  private ProjectManifest projectManifest;
  private Integer enforcedAppVersion;

  public DeploymentBuilderImpl(RepositoryServiceImpl repositoryService) {
    this(repositoryService,
         Context.getProcessEngineConfiguration().getDeploymentEntityManager().create(),
         Context.getProcessEngineConfiguration().getResourceEntityManager());
  }

  public DeploymentBuilderImpl(RepositoryServiceImpl repositoryService,
                               DeploymentEntity deployment,
                               ResourceEntityManager resourceEntityManager) {
    this.repositoryService = repositoryService;
    this.deployment = deployment;
    this.resourceEntityManager = resourceEntityManager;
  }

  public DeploymentBuilder addInputStream(String resourceName, InputStream inputStream) {
    if (inputStream == null) {
      throw new ActivitiIllegalArgumentException("inputStream for resource '" + resourceName + "' is null");
    }
    byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
    ResourceEntity resource = resourceEntityManager.create();
    resource.setName(resourceName);
    resource.setBytes(bytes);
    deployment.addResource(resource);
    return this;
  }

  public DeploymentBuilder setProjectManifest(ProjectManifest projectManifest) {
      this.projectManifest = projectManifest;
      return this;
  }

  public ProjectManifest getProjectManifest(){
      return this.projectManifest;
  }

  public boolean hasProjectManifestSet(){
      return this.projectManifest != null;
  }

  public DeploymentBuilder setEnforcedAppVersion(Integer enforcedAppVersion){
      this.enforcedAppVersion = enforcedAppVersion;
      return this;
  }

  public Integer getEnforcedAppVersion(){
      return this.enforcedAppVersion;
  }

  public boolean hasEnforcedAppVersion(){
      return this.enforcedAppVersion != null;
  }

  @Override
  public DeploymentBuilder addInputStream(String resourceName,
                                          Resource resource) {
    try {
      if (resourceName.endsWith(".bar") || resourceName.endsWith(".zip") || resourceName.endsWith(".jar")) {
        try(ZipInputStream inputStream = new ZipInputStream(resource.getInputStream())) {
          addZipInputStream(inputStream);
        }
      } else {
        try(InputStream inputStream = resource.getInputStream()) {
          addInputStream(resourceName,
                         inputStream);
        }
      }
    } catch (IOException e) {
      throw new ActivitiException("Couldn't auto deploy resource '" + resource + "': " + e.getMessage(), e);
    }
    return this;
  }

  public DeploymentBuilder addClasspathResource(String resource) {
    InputStream inputStream = ReflectUtil.getResourceAsStream(resource);
    if (inputStream == null) {
      throw new ActivitiIllegalArgumentException("resource '" + resource + "' not found");
    }
    return addInputStream(resource, inputStream);
  }

  public DeploymentBuilder addString(String resourceName, String text) {
    if (text == null) {
      throw new ActivitiIllegalArgumentException("text is null");
    }
    ResourceEntity resource = resourceEntityManager.create();
    resource.setName(resourceName);
    try {
      resource.setBytes(text.getBytes(DEFAULT_ENCODING));
    } catch (UnsupportedEncodingException e) {
      throw new ActivitiException("Unable to get process bytes.", e);
    }
    deployment.addResource(resource);
    return this;
  }
  
  public DeploymentBuilder addBytes(String resourceName, byte[] bytes) {
    if (bytes == null) {
      throw new ActivitiIllegalArgumentException("bytes is null");
    }
    ResourceEntity resource = resourceEntityManager.create();
    resource.setName(resourceName);
    resource.setBytes(bytes);
    
    deployment.addResource(resource);
    return this;
  }

  public DeploymentBuilder addZipInputStream(ZipInputStream zipInputStream) {
    try {
      ZipEntry entry = zipInputStream.getNextEntry();
      while (entry != null) {
        if (!entry.isDirectory()) {
          String entryName = entry.getName();
          byte[] bytes = IoUtil.readInputStream(zipInputStream, entryName);
          ResourceEntity resource = resourceEntityManager.create();
          resource.setName(entryName);
          resource.setBytes(bytes);
          deployment.addResource(resource);
        }
        entry = zipInputStream.getNextEntry();
      }
    } catch (Exception e) {
      throw new ActivitiException("problem reading zip input stream", e);
    }
    return this;
  }

  public DeploymentBuilder addBpmnModel(String resourceName, BpmnModel bpmnModel) {
    BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
    try {
      String bpmn20Xml = new String(bpmnXMLConverter.convertToXML(bpmnModel), "UTF-8");
      addString(resourceName, bpmn20Xml);
    } catch (UnsupportedEncodingException e) {
      throw new ActivitiException("Error while transforming BPMN model to xml: not UTF-8 encoded", e);
    }
    return this;
  }

  public DeploymentBuilder name(String name) {
    deployment.setName(name);
    return this;
  }

  public DeploymentBuilder category(String category) {
    deployment.setCategory(category);
    return this;
  }
  
  public DeploymentBuilder key(String key) {
    deployment.setKey(key);
    return this;
  }

  public DeploymentBuilder disableBpmnValidation() {
    this.isProcessValidationEnabled = false;
    return this;
  }

  public DeploymentBuilder disableSchemaValidation() {
    this.isBpmn20XsdValidationEnabled = false;
    return this;
  }

  public DeploymentBuilder tenantId(String tenantId) {
    deployment.setTenantId(tenantId);
    return this;
  }

  public DeploymentBuilder enableDuplicateFiltering() {
    this.isDuplicateFilterEnabled = true;
    return this;
  }

  public DeploymentBuilder activateProcessDefinitionsOn(Date date) {
    this.processDefinitionsActivationDate = date;
    return this;
  }
  
  @Override
  public DeploymentBuilder deploymentProperty(String propertyKey, Object propertyValue) {
    deploymentProperties.put(propertyKey, propertyValue);
    return this;
  }

  public Deployment deploy() {
    return repositoryService.deploy(this);
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public DeploymentEntity getDeployment() {
    return deployment;
  }

  public boolean isProcessValidationEnabled() {
    return isProcessValidationEnabled;
  }

  public boolean isBpmn20XsdValidationEnabled() {
    return isBpmn20XsdValidationEnabled;
  }

  public boolean isDuplicateFilterEnabled() {
    return isDuplicateFilterEnabled;
  }

  public Date getProcessDefinitionsActivationDate() {
    return processDefinitionsActivationDate;
  }

  public Map<String, Object> getDeploymentProperties() {
    return deploymentProperties;
  }
  
}
