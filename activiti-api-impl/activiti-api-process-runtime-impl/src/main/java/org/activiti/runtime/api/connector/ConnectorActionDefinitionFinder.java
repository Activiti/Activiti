package org.activiti.runtime.api.connector;

import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.model.connector.ActionDefinition;
import org.activiti.model.connector.ConnectorDefinition;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConnectorActionDefinitionFinder{

    public Optional<ActionDefinition> find(String implementation, List<ConnectorDefinition> connectorDefinitions){

        Optional<ActionDefinition> actionDefinitionOptional = Optional.empty();

        String connectorId = StringUtils.substringBefore(implementation, ".");
        String actionId = StringUtils.substringAfter(implementation, ".");

        List<ConnectorDefinition> resultingConnectors = connectorDefinitions.stream().filter(c -> c.getId().equals(connectorId)).collect(Collectors.toList());
        if (resultingConnectors != null && resultingConnectors.size() != 0) {
            if (resultingConnectors.size() != 1) {
                throw new RuntimeException("Mismatch connector id mapping: " + connectorId);
            }
            ConnectorDefinition connectorDefinition = resultingConnectors.get(0);

            ActionDefinition actionDefinition = connectorDefinition.getActions().get(actionId);
            if (actionDefinition == null) {
                throw new RuntimeException("Mismatch action id mapping: " + actionId);
            }

            actionDefinitionOptional = Optional.ofNullable(actionDefinition);
        }

        return actionDefinitionOptional;
    }

}
