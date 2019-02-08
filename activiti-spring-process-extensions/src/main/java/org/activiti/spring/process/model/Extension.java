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

public class Extension {

    private Map<String, VariableDefinition> properties = new HashMap<>();
    private Map<String, ProcessVariablesMapping> mappings = new HashMap<>();

    private final ProcessVariablesMapping EMPTY_PROCESS_VARIABLES_MAPPING = new ProcessVariablesMapping();

    public Map<String, VariableDefinition> getProperties() {
        return properties;
    }
    public void setProperties(Map<String, VariableDefinition> properties) {
        this.properties = properties;
    }

    public Map<String, ProcessVariablesMapping> getMappings() {
        return mappings;
    }

    public ProcessVariablesMapping getMappingForFlowElement(String flowElementUUID) {
        ProcessVariablesMapping processVariablesMapping = mappings.get(flowElementUUID);
        return processVariablesMapping != null? processVariablesMapping : EMPTY_PROCESS_VARIABLES_MAPPING;
    }

    public void setMappings(Map<String, ProcessVariablesMapping> mappings) {
        this.mappings = mappings;
    }

    public VariableDefinition getProperty(String propertyUUID){
        return properties != null? properties.get(propertyUUID) : null;
    }
}
