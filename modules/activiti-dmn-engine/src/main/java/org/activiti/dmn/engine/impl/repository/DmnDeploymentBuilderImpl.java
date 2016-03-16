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
package org.activiti.dmn.engine.impl.repository;

import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.activiti.dmn.engine.ActivitiDmnException;
import org.activiti.dmn.engine.domain.entity.DmnDeployment;
import org.activiti.dmn.engine.domain.entity.DmnDeploymentResource;
import org.activiti.dmn.engine.impl.DmnRepositoryServiceImpl;
import org.activiti.dmn.engine.repository.DmnDeploymentBuilder;
import org.activiti.dmn.model.DmnDefinition;
import org.activiti.dmn.xml.converter.DmnXMLConverter;
import org.apache.commons.io.IOUtils;

/**
 * @author Tijs Rademakers
 */
public class DmnDeploymentBuilderImpl implements DmnDeploymentBuilder, Serializable {

    private static final long serialVersionUID = 1L;
    protected static final String DEFAULT_ENCODING = "UTF-8";

    protected transient DmnRepositoryServiceImpl repositoryService;
    protected DmnDeployment deployment = new DmnDeployment();
    protected Map<String, DmnDeploymentResource> resourceMap = new HashMap<String, DmnDeploymentResource>();
    protected boolean isDmn20XsdValidationEnabled = true;

    public DmnDeploymentBuilderImpl(DmnRepositoryServiceImpl repositoryService) {
        this.repositoryService = repositoryService;
    }

    public DmnDeploymentBuilder addInputStream(String resourceName, InputStream inputStream) {
        if (inputStream == null) {
            throw new ActivitiDmnException("inputStream for resource '" + resourceName + "' is null");
        }

        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            DmnDeploymentResource resource = new DmnDeploymentResource();
            resource.setName(resourceName);
            resource.setResourceBytes(bytes);
            resourceMap.put(resourceName, resource);
            return this;

        } catch (Exception e) {
            throw new ActivitiDmnException("Resource '" + resourceName + "' can't be created", e);
        }
    }

    public DmnDeploymentBuilder addClasspathResource(String resource) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resource);
        if (inputStream == null) {
            throw new ActivitiDmnException("resource '" + resource + "' not found");
        }
        return addInputStream(resource, inputStream);
    }

    public DmnDeploymentBuilder addString(String resourceName, String text) {
        if (text == null) {
            throw new ActivitiDmnException("text is null");
        }

        DmnDeploymentResource resource = new DmnDeploymentResource();
        resource.setName(resourceName);

        try {
            resource.setResourceBytes(text.getBytes(DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new ActivitiDmnException("Unable to get decision bytes.", e);
        }

        resourceMap.put(resourceName, resource);

        return this;
    }

    public DmnDeploymentBuilder addDmnModel(String resourceName, DmnDefinition dmnDefinition) {
        DmnXMLConverter dmnXMLConverter = new DmnXMLConverter();
        try {
            String dmn20Xml = new String(dmnXMLConverter.convertToXML(dmnDefinition), "UTF-8");
            addString(resourceName, dmn20Xml);
        } catch (UnsupportedEncodingException e) {
            throw new ActivitiDmnException("Error while transforming DMN model to xml: not UTF-8 encoded", e);
        }
        return this;
    }

    public DmnDeploymentBuilder name(String name) {
        deployment.setName(name);
        return this;
    }

    public DmnDeploymentBuilder category(String category) {
        deployment.setCategory(category);
        return this;
    }

    public DmnDeploymentBuilder disableSchemaValidation() {
        this.isDmn20XsdValidationEnabled = false;
        return this;
    }

    public DmnDeploymentBuilder tenantId(String tenantId) {
        deployment.setTenantId(tenantId);
        return this;
    }

    public DmnDeployment deploy() {
        deployment.setNew(true);
        return repositoryService.deploy(this);
    }

    // getters and setters
    // //////////////////////////////////////////////////////

    public DmnDeployment getDeployment() {
        return deployment;
    }

    public Map<String, DmnDeploymentResource> getResourceMap() {
        return resourceMap;
    }

    public boolean isDmnXsdValidationEnabled() {
        return isDmn20XsdValidationEnabled;
    }

}
