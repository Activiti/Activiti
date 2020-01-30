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

package org.activiti.spring.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.resources.DeploymentResourceLoader;

public class ProcessExtensionService {

    private DeploymentResourceLoader<ProcessExtensionModel> processExtensionLoader;
    private ProcessExtensionResourceReader processExtensionReader;
    private RepositoryService repositoryService;

    private static final Extension EMPTY_EXTENSIONS = new Extension();
    private Map<String, Map<String, ProcessExtensionModel>> processExtensionModelDeploymentMap = new HashMap<>();

    public ProcessExtensionService(DeploymentResourceLoader<ProcessExtensionModel> processExtensionLoader,
                                   ProcessExtensionResourceReader processExtensionReader) {

        this.processExtensionLoader = processExtensionLoader;
        this.processExtensionReader = processExtensionReader;
    }

    private Map<String, ProcessExtensionModel> getProcessExtensionsForDeploymentId(String deploymentId, String processDefinitionKey) {
        Map<String, ProcessExtensionModel> processExtensionModelMap = processExtensionModelDeploymentMap.get(deploymentId);
        if (processExtensionModelMap != null) {
            return processExtensionModelMap;
        }

        List<ProcessExtensionModel> processExtensionModels = processExtensionLoader.loadResourcesForDeployment(deploymentId,
                processExtensionReader);

        processExtensionModelMap = this.buildProcessDefinitionAndExtensionMap(processExtensionModels, processDefinitionKey);
        processExtensionModelDeploymentMap.put(deploymentId, processExtensionModelMap);
        return processExtensionModelMap;
    }

    private Map<String, ProcessExtensionModel> buildProcessDefinitionAndExtensionMap(List<ProcessExtensionModel> processExtensionModels,
                                                                                     String processDefinitionKey){
        Map<String, ProcessExtensionModel> buildProcessExtensionMap = null;
        if(processExtensionModels.size() > 0 ){
            buildProcessExtensionMap = new HashMap();
            for(ProcessExtensionModel processExtensionModel:processExtensionModels ){
                if(processExtensionModel.getExtensions(processDefinitionKey) != null){
                    buildProcessExtensionMap.put(processDefinitionKey, processExtensionModel);
                }
            }

        }
        return buildProcessExtensionMap;
    }

    public boolean hasExtensionsFor(ProcessDefinition processDefinition) {
        return !EMPTY_EXTENSIONS.equals(getExtensionsFor(processDefinition));
    }

    public boolean hasExtensionsFor(String processDefinitionId) {
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
        return hasExtensionsFor(processDefinition);
    }

    public Extension getExtensionsFor(ProcessDefinition processDefinition) {
        ProcessExtensionModel processExtensionModel = null;

        Map<String, ProcessExtensionModel> processExtensionModelMap = getProcessExtensionsForDeploymentId(processDefinition.getDeploymentId(),
                                                                                                          processDefinition.getKey());
        if (processExtensionModelMap != null) {
            processExtensionModel = processExtensionModelMap.get(processDefinition.getKey());
        }

        return processExtensionModel != null ? processExtensionModel.getExtensions(processDefinition.getKey()) : EMPTY_EXTENSIONS;
    }

    public Extension getExtensionsForId(String processDefinitionId) {
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);

        Extension processExtension = getExtensionsFor(processDefinition);
        return processExtension != null ? processExtension : EMPTY_EXTENSIONS;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }
}
