/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.spring.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.types.VariableType;
import org.activiti.spring.resources.ResourceReader;

public class ProcessExtensionResourceReader implements ResourceReader<ProcessExtensionModel> {

    private final ObjectMapper objectMapper;
    private final Map<String, VariableType> variableTypeMap;

    public ProcessExtensionResourceReader(ObjectMapper objectMapper,
                                          Map<String, VariableType> variableTypeMap) {
        this.objectMapper = objectMapper;
        this.variableTypeMap = variableTypeMap;
    }

    @Override
    public Predicate<String> getResourceNameSelector() {
        return resourceName -> resourceName.endsWith("-extensions.json");
    }

    @Override
    public ProcessExtensionModel read(InputStream inputStream) throws IOException {
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        ProcessExtensionModel mappedModel = objectMapper.readValue(inputStream,
                                                                   ProcessExtensionModel.class);

        return convertJsonVariables(mappedModel);
    }

    /**
     * Json variables need to be represented as JsonNode for engine to handle as Json
     * Do this for any var marked as json or whose type is not recognised from the extension file
     */
    private ProcessExtensionModel convertJsonVariables(ProcessExtensionModel processExtensionModel) {
        if (processExtensionModel != null && processExtensionModel.getExtensions() != null
                && processExtensionModel.getExtensions().getProperties() != null) {

            for (VariableDefinition variableDefinition : processExtensionModel.getExtensions().getProperties().values()) {
                if (!variableTypeMap.keySet().contains(variableDefinition.getType()) || variableDefinition.getType().equals("json")) {
                    variableDefinition.setValue(objectMapper.convertValue(variableDefinition.getValue(),
                                                                          JsonNode.class));
                }
            }
        }
        return processExtensionModel;
    }
}
