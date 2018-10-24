package org.activiti.runtime.api.connector;

import org.activiti.core.common.model.connector.VariableDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariablesMatchHelper {

    public Map<String, Object> match(Map<String, Object> results, List<VariableDefinition> variableDefinitions) {

        if (variableDefinitions != null && !variableDefinitions.isEmpty()) {
            Map<String, Object> variables = new HashMap<>();
            for (VariableDefinition variableDefinition : variableDefinitions) {
                Object outBoundVariableValue = results.get(variableDefinition.getName());
                if (outBoundVariableValue != null) {
                    variables.put(variableDefinition.getName(), outBoundVariableValue);
                }
            }
            return variables;
        }
        return results;
    }
}