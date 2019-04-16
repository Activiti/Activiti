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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.core.io.Resource;

/**
 * Default implementation of {@link AutoDeploymentStrategy} that groups all {@link Resource}s into a single deployment. This implementation is equivalent to the previously used implementation.
 * 

 */
public class DefaultAutoDeploymentStrategy extends AbstractAutoDeploymentStrategy {

  /**
   * The deployment mode this strategy handles.
   */
  public static final String DEPLOYMENT_MODE = "default";
  
  private Map<String, String> deployedProcess = new HashMap<>();
  
  @Override
  protected String getDeploymentMode() {
    return DEPLOYMENT_MODE;
  }

  @Override
  public void deployResources(final String deploymentNameHint, final RepositoryService repositoryService) {

    // Create a single deployment for all resources using the name hint as
    // the
    // literal name
    if (processDefinitionResources == null || processDefinitionResources.length <1) {
        return;
    }
//    final DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().enableDuplicateFiltering().name(deploymentNameHint);
//
//    for (final Resource resource : processDefinitionResources) {
//      final String resourceName = determineResourceName(resource);
//      
//      
//
//      deploymentBuilder.addInputStream(resourceName,
//                                       resource);
//    }
//
//    deploymentBuilder.deploy();

        for (final Resource resource : processDefinitionResources) {
            Entry<String, String> deployEntry = deployProcessFromResource(deploymentNameHint,
                                                                          repositoryService, 
                                                                          resource);
            if (deployEntry != null) {
                deployedProcess.put(deployEntry.getKey(), deployEntry.getValue());
            }
        }
    
    
  }
  
  private Entry<String, String> deployProcessFromResource(final String deploymentNameHint,
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
