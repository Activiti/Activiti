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

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.ProcessInstanceHelper;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.VariableDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessVariablesInitiator extends ProcessInstanceHelper {

    @Autowired
    private Map<String, ProcessExtensionModel> processExtensionDefinitionMap;

    @Override
    public ProcessInstance createAndStartProcessInstanceWithInitialFlowElement(ProcessDefinition processDefinition, String businessKey, String processInstanceName, FlowElement initialFlowElement, Process process, Map<String, Object> variables, Map<String, Object> transientVariables, boolean startProcessInstance) {
        if (processExtensionDefinitionMap.containsKey(processDefinition.getKey())) {
            ProcessExtensionModel processExtensionModel = processExtensionDefinitionMap.get(processDefinition.getKey());
            Map<String, VariableDefinition> variableDefinitionMap = processExtensionModel.getExtensions().getProperties();
            variables = processVariables(variables, variableDefinitionMap);
            Set<String> missingRequiredVars = checkRequiredVariables(variables, variableDefinitionMap);
            if (!missingRequiredVars.isEmpty()) {
                throw new ActivitiException("Can't start process '" + processDefinition.getKey() + "' without required variables " + String.join(", ", missingRequiredVars));
            }
            Set<String> varsWithMismatchedTypes = checkVariablesMatchTypes(variables,variableDefinitionMap);
            if(!varsWithMismatchedTypes.isEmpty()){
                throw new ActivitiException("Can't start process '" + processDefinition.getKey() + "' as variables have unexpected types " + String.join(", ", varsWithMismatchedTypes));
            }
        }
        return super.createAndStartProcessInstanceWithInitialFlowElement(processDefinition, businessKey, processInstanceName, initialFlowElement, process, variables, transientVariables, startProcessInstance);
    }

    private Map<String, Object> processVariables(Map<String, Object> variables, Map<String, VariableDefinition> variableDefinitionMap) {
        Map<String, Object> newVarsMap = new HashMap<>(variables);
        variableDefinitionMap.forEach((k,v) -> {
            if (!newVarsMap.containsKey(v.getName()) && v.getValue() != null) {
                newVarsMap.put(v.getName(), createDefaultVaribleValue(v));
            }
        });
        return newVarsMap;
    }

    private Object createDefaultVaribleValue(VariableDefinition variableDefinition) {
        if (ExtensionVariableTypes.DATE.name.equals(variableDefinition.getType())){
            try {
                return ExtensionVariableTypes.DATE.getDateFormat().parse(String.valueOf(variableDefinition.getValue()));
            } catch (ParseException e) {
                throw new ActivitiException("Error parsing date variable '" + variableDefinition.getName()
                         + "' with value " + variableDefinition.getValue(), e);
            }
        }
        return variableDefinition.getValue();
    }

    private Set<String> checkRequiredVariables(Map<String, Object> variables, Map<String, VariableDefinition> variableDefinitionMap) {
        Set<String> missingRequiredVars = new HashSet<>();
        variableDefinitionMap.forEach((k,v) -> {
            if (!variables.containsKey(v.getName()) && v.isRequired()) {
                missingRequiredVars.add(v.getName());
            }
        });
        return missingRequiredVars;
    }

    private Set<String> checkVariablesMatchTypes(Map<String, Object> variables, Map<String, VariableDefinition> variableDefinitionMap) {
        Set<String> mismatchedVars = new HashSet<>();
        variableDefinitionMap.forEach((k,v) -> {
            if (variables.containsKey(v.getName()) ) {
                ExtensionVariableTypes type = ExtensionVariableTypes.getEnumByString(v.getType());
                boolean eligibleClass = false;
                for (Class clazz:type.classes){
                    if(variables.get(v.getName()).getClass().isAssignableFrom(clazz)){
                        eligibleClass = true;
                    }
                }
                if (!eligibleClass){
                    mismatchedVars.add(v.getName());
                }
            }
        });
        return mismatchedVars;
    }

}