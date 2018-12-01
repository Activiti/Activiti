package org.activiti.runtime.api.connector;

import org.activiti.core.common.model.connector.VariableDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.VariableMapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.function.Function;
import java.util.stream.Collectors;

public class VariablesMatchHelper {

    @Autowired
    private Map<String, ProcessExtensionModel> processExtensionDefinitionMap;

    private MappingProvider inputMappingProvider = new InputMappingProvider();
    private MappingProvider outputMappingProvider = new OutputMappingProvider();

    public Map<String, Object> matchInput(DelegateExecution execution, Map<String, Object> results,
                                          List<VariableDefinition> variableDefinitions) {
        return match(execution, results, variableDefinitions, inputMappingProvider);
    }

    public Map<String, Object> matchOutput(DelegateExecution execution, Map<String, Object> results,
                                           List<VariableDefinition> variableDefinitions) {
        return match(execution, results, variableDefinitions, outputMappingProvider);
    }

    private Map<String, Object> match(DelegateExecution execution, Map<String, Object> results,
                                        List<VariableDefinition> variableDefinitions, MappingProvider mappingProvider) {

        if (variableDefinitions != null && !variableDefinitions.isEmpty()) {
            Optional<ProcessExtensionModel> optionalProcessExtensionModel = getProcessExtension(execution);
            if (optionalProcessExtensionModel.isPresent()) {
                ProcessExtensionModel processExtensionModel = optionalProcessExtensionModel.get();
                VariableMapping variableMapping = processExtensionModel.getExtensions()
                        .getVariablesMappings()
                        .get(execution.getCurrentActivityId());
                if (variableMapping != null) {
                    return matchWithMapping(results, mappingProvider.getMappings(variableMapping),
                            convertToMap(variableDefinitions),
                            processExtensionModel.getExtensions().getProperties(),
                            mappingProvider.getValueProvider());
                }
            }
            return matchNoMapping(results, variableDefinitions);
        }
        return results;
    }

    private Map<String, Object> matchWithMapping(Map<String, Object> results, Map<String, String> mappings,
                                                 Map<String, ? extends VariableDefinition> connectorVariablesMap,
                                                 Map<String, ? extends VariableDefinition> variableDefinitionMap,
                                                 MappedValueProvider mappedValueProvider) {

        Map<String, Object> variables = new HashMap<>();
        connectorVariablesMap.forEach((k, v) -> {
            if (mappings.containsKey(k)) {
                String mappedVariableId = mappings.get(v.getId());
                VariableDefinition mappedVariableDefinition = variableDefinitionMap.get(mappedVariableId);
                if (mappedVariableDefinition != null) {
                    Optional<Object> outBoundVariableValueOptional = mappedValueProvider.getValue(results, v, mappedVariableDefinition);
                    if (outBoundVariableValueOptional.isPresent()) {
                        variables.put(mappedVariableDefinition.getName(), outBoundVariableValueOptional.get());
                    }
                }
                // TODO what if the mapped variable definition wasn't found?
            } else {
                Object outBoundVariableValue = results.get(v.getName());
                if (outBoundVariableValue != null) {
                    variables.put(v.getName(), outBoundVariableValue);
                }
            }
        });
        return variables;
    }

    private Map<String, Object> matchNoMapping(Map<String, Object> results, List<VariableDefinition> variableDefinitions) {
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

    private Optional<ProcessExtensionModel> getProcessExtension(DelegateExecution execution) {
        String processDefinitionKey = execution.getProcessDefinitionId().split(":")[0];
        return Optional.ofNullable(processExtensionDefinitionMap.get(processDefinitionKey));
    }

    private Map<String, VariableDefinition> convertToMap(List<VariableDefinition> variableDefinitions) {
        return variableDefinitions.stream().collect(Collectors.toMap(VariableDefinition::getId, Function.identity()));
    }

    interface MappingProvider {
        Map<String, String> getMappings(VariableMapping variableMapping);
        MappedValueProvider getValueProvider();
    }

    class InputMappingProvider implements MappingProvider {
        private MappedValueProvider inputMappedValueProvider = new InputMappedValueProvider();
        public Map<String, String> getMappings(VariableMapping variableMapping) {
            return variableMapping.getInput();
        }
        public MappedValueProvider getValueProvider() {
            return inputMappedValueProvider;
        }
    }

    class OutputMappingProvider implements MappingProvider {
        private MappedValueProvider outputMappedValueProvider = new OutputMappedValueProvider();
        public Map<String, String> getMappings(VariableMapping variableMapping) {
            return variableMapping.getOutput();
        }
        public MappedValueProvider getValueProvider() {
            return outputMappedValueProvider;
        }
    }

    interface MappedValueProvider {
        Optional<Object> getValue(Map<String, Object> results, VariableDefinition variableDefinition,
                                  VariableDefinition mappedVariableDefinition);
    }

    class InputMappedValueProvider implements MappedValueProvider {
        public Optional<Object> getValue(Map<String, Object> results, VariableDefinition variableDefinition,
                                  VariableDefinition mappedVariableDefinition) {
            return Optional.ofNullable(results.get(mappedVariableDefinition.getName()));
        }
    }

    class OutputMappedValueProvider implements MappedValueProvider {
        public Optional<Object> getValue(Map<String, Object> results, VariableDefinition variableDefinition,
                                  VariableDefinition mappedVariableDefinition) {
            return Optional.ofNullable(results.get(variableDefinition.getName()));
        }
    }
}