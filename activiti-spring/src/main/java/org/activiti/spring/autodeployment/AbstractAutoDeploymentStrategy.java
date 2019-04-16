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

package org.activiti.spring.autodeployment;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.validation.ValidationError;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;

/**
 * Abstract base class for implementations of {@link AutoDeploymentStrategy}.
 * 
 * 
 */
public abstract class AbstractAutoDeploymentStrategy implements AutoDeploymentStrategy {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractAutoDeploymentStrategy.class);

    /**
     * Gets the deployment mode this strategy handles.
     * 
     * @return the name of the deployment mode
     */
    protected abstract String getDeploymentMode();
    
    protected Resource[] processDefinitionResources = new Resource[0];
    protected Map<String, Resource> processExtensionResources = new HashMap<>();
    
    public void setProcessDefinitionResources(final Resource[] processDefinitionResources) {
        this.processDefinitionResources = processDefinitionResources;
    } 
    public void setProcessExtensionResources(final Map<String, Resource> processExtensionResources) {
        this.processExtensionResources = processExtensionResources;
        
    }
    
    public void deployResources(final String deploymentNameHint, Resource[] resources, final RepositoryService repositoryService) {

        setProcessDefinitionResources(resources);
        deployResources(deploymentNameHint,repositoryService);
    }
    

    @Override
    public boolean handlesMode(final String mode) {
        return StringUtils.equalsIgnoreCase(mode, getDeploymentMode());
    }

    /**
     * Determines the name to be used for the provided resource.
     * 
     * @param resource the resource to get the name for
     * @return the name of the resource
     */
    protected String determineResourceName(final Resource resource) {
        String resourceName = null;

        if (resource instanceof ContextResource) {
            resourceName = ((ContextResource) resource).getPathWithinContext();

        } else if (resource instanceof ByteArrayResource) {
            resourceName = resource.getDescription();

        } else {
            try {
                resourceName = resource.getFile().getAbsolutePath();
            } catch (IOException e) {
                resourceName = resource.getFilename();
            }
        }
        return resourceName;
    }

    protected boolean validateModel(Resource resource, final RepositoryService repositoryService) {
        try {
            BpmnXMLConverter converter = new BpmnXMLConverter();
            BpmnModel bpmnModel = converter.convertToBpmnModel(new InputStreamSource(resource.getInputStream()), true,
                    false);
            List<ValidationError> validationErrors = repositoryService.validateProcess(bpmnModel);
            if ( validationErrors != null && !validationErrors.isEmpty() ) {
                StringBuilder warningBuilder = new StringBuilder();
                StringBuilder errorBuilder = new StringBuilder();

                for (ValidationError error : validationErrors) {
                    if ( error.isWarning() ) {
                        warningBuilder.append(error.toString());
                        warningBuilder.append("\n");
                    } else {
                        errorBuilder.append(error.toString());
                        errorBuilder.append("\n");
                    }

                    // Write out warnings (if any)
                    if ( warningBuilder.length() > 0 ) {
                        LOGGER.warn("Following warnings encountered during process validation: "
                                + warningBuilder.toString());
                    }

                    if ( errorBuilder.length() > 0 ) {
                        LOGGER.error("Errors while parsing:\n" + errorBuilder.toString());
                        return false;
                    }
                }
            }
        } catch ( Exception e ) {
            LOGGER.error("Error parsing XML", e);
            return false;
        }
        return true;
    }
    
    protected BpmnModel getBpmnModelFromProcessDefinitionResource(Resource resource) throws Exception {
        assertNotNull(resource);
        return getBpmnModelFromInputStream(resource.getInputStream());
    }
    
    protected BpmnModel getBpmnModelFromInputStream(InputStream stream) throws Exception {
        assertNotNull(stream);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(stream, "UTF-8");
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        return  new BpmnXMLConverter().convertToBpmnModel(xtr);
     }
    
    protected Entry<String, String> deployProcessFromResource(final String deploymentNameHint,
                                                              final RepositoryService repositoryService,
                                                              Resource xmlResource) {
                       
       //Check / get BpmnModel  
        BpmnModel bpmnModel; 
        try {
            bpmnModel = getBpmnModelFromProcessDefinitionResource(xmlResource);
            assertNotNull(bpmnModel);
        } catch (Exception e) {
            return null;
        }
       
       
       //Get main process
       Process process = bpmnModel.getMainProcess();
        
       //Find Extensions for our process
       Resource processExtensionResource = processExtensionResources.get(process.getId());
       
       //Deploy process       
       DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                                                   .enableDuplicateFiltering()
                                                   .name(deploymentNameHint)
                                                   .key(process.getId());

       deploymentBuilder.addBpmnModel(xmlResource.getFilename(), bpmnModel);
       
       //Add process extensions (as resource)
       if (processExtensionResource != null) {
           deploymentBuilder.addInputStream(processExtensionResource.getFilename(), processExtensionResource);
           
       }
       
       Deployment deployment = deploymentBuilder.deploy();
       assertNotNull(deployment);
       
       return new AbstractMap.SimpleEntry<String, String>(deployment.getId(),process.getId());
    }
    
}
