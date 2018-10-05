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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProcessExtensionService {

    private String processExtensionsRoot;
    private String processExtensionsSuffix;
    private final ObjectMapper objectMapper;
    private ResourcePatternResolver resourceLoader;

    public ProcessExtensionService(String processExtensionsRoot, String processExtensionsSuffix,
                                   ObjectMapper objectMapper, ResourcePatternResolver resourceLoader) {
        this.processExtensionsRoot = processExtensionsRoot;
        this.processExtensionsSuffix = processExtensionsSuffix;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    private Optional<Resource[]> retrieveResources() throws IOException {
        Optional<Resource[]> resources = Optional.empty();
        Resource processExtensionsResource = resourceLoader.getResource(processExtensionsRoot);
        if (processExtensionsResource.exists()) {
            return Optional.ofNullable(resourceLoader.getResources(processExtensionsRoot + processExtensionsSuffix));
        }
        return resources;
    }

    private ProcessExtensionModel read(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream,
                ProcessExtensionModel.class);
    }

    public Map<String, ProcessExtensionModel> get() throws IOException {
        List<ProcessExtensionModel> processExtensionModels = new ArrayList<>();
        Optional<Resource[]> resourcesOptional = retrieveResources();
        if (resourcesOptional.isPresent()) {
            for (Resource resource : resourcesOptional.get()) {
                processExtensionModels.add(read(resource.getInputStream()));
            }
        }
        return convertToMap(processExtensionModels);
    }

    private Map<String, ProcessExtensionModel> convertToMap(List<ProcessExtensionModel> processExtensionModelList){
        return processExtensionModelList.stream()
                .collect(Collectors.toMap(ProcessExtensionModel::getId,
                        Function.identity()));
    }
}
