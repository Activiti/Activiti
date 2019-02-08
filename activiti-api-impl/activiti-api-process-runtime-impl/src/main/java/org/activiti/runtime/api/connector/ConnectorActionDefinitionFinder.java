package org.activiti.runtime.api.connector;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.apache.commons.lang3.StringUtils;

public class ConnectorActionDefinitionFinder {

    private final List<ConnectorDefinition> connectorDefinitions;

    public ConnectorActionDefinitionFinder(List<ConnectorDefinition> connectorDefinitions) {
        this.connectorDefinitions = connectorDefinitions;
    }

    public Optional<ActionDefinition> find(String implementation){

        Optional<ActionDefinition> actionDefinitionOptional = Optional.empty();

        String connectorId = StringUtils.substringBefore(implementation,
                                                         ".");
        String actionId = StringUtils.substringAfter(implementation,
                                                     ".");

        List<ConnectorDefinition> resultingConnectors = connectorDefinitions.stream().filter(c -> c.getId().equals(connectorId)).collect(Collectors.toList());
        if (resultingConnectors != null && resultingConnectors.size() != 0) {
            if (resultingConnectors.size() != 1) {
                throw new RuntimeException("Expecting exactly 1 connector definition with id mapping `" + connectorId +
                                                   "`, but were found " + resultingConnectors.size());
            }
            ConnectorDefinition connectorDefinition = resultingConnectors.get(0);

            ActionDefinition actionDefinition = connectorDefinition.getActions().get(actionId);
            if (actionDefinition == null) {
                throw new RuntimeException("No action with id mapping `" + actionId + "` was found in connector `" +
                                                   connectorId + "`");
            }

            actionDefinitionOptional = Optional.of(actionDefinition);
        }

        return actionDefinitionOptional;
    }
}
