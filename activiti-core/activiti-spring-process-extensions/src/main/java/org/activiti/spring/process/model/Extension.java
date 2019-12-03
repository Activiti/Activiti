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

package org.activiti.spring.process.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Extension {

    private final ProcessVariablesMapping EMPTY_PROCESS_VARIABLES_MAPPING = new ProcessVariablesMapping();
    private Map<String, VariableDefinition> properties = new HashMap<>();
    private Map<String, ProcessVariablesMapping> mappings = new HashMap<>();
    private Map<String, ProcessConstantsMapping> constants = new HashMap<>();

    public Map<String, VariableDefinition> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, VariableDefinition> properties) {
        this.properties = properties;
    }

    public Map<String, ProcessVariablesMapping> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, ProcessVariablesMapping> mappings) {
        this.mappings = mappings;
    }

    public Map<String, ProcessConstantsMapping> getConstants() {
        return constants;
    }

    public void setConstants(Map<String, ProcessConstantsMapping> constants) {
        this.constants = constants;
    }


    public ProcessConstantsMapping getConstantForFlowElement(String flowElementUUID) {
        ProcessConstantsMapping processConstantsMapping = constants.get(flowElementUUID);
        return processConstantsMapping != null ? processConstantsMapping : new ProcessConstantsMapping();
    }


    public ProcessVariablesMapping getMappingForFlowElement(String flowElementUUID) {
        ProcessVariablesMapping processVariablesMapping = mappings.get(flowElementUUID);
        return processVariablesMapping != null ? processVariablesMapping : EMPTY_PROCESS_VARIABLES_MAPPING;
    }

    public VariableDefinition getProperty(String propertyUUID) {
        return properties != null ? properties.get(propertyUUID) : null;
    }

    public VariableDefinition getPropertyByName(String name) {
        if (properties != null) {
            for (Map.Entry<String, VariableDefinition> variableDefinition : properties.entrySet()) {
                if (variableDefinition.getValue() != null) {
                    if (Objects.equals(variableDefinition.getValue().getName(), name)) {
                        return variableDefinition.getValue();
                    }
                }
            }
        }

        return null;
    }

    public boolean hasEmptyInputsMapping(String elementId) {
        ProcessVariablesMapping processVariablesMapping = mappings.get(elementId);
        return processVariablesMapping != null && processVariablesMapping.getInputs().size() == 0;
    }

    public boolean hasEmptyOutputsMapping(String elementId) {
        ProcessVariablesMapping processVariablesMapping = mappings.get(elementId);
        return processVariablesMapping != null && processVariablesMapping.getOutputs().size() == 0;
    }

    public boolean hasMapping(String taskId) {
        return mappings.get(taskId) != null;
    }


}
