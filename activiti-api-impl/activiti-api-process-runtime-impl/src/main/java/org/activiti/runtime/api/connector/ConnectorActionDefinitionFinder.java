package org.activiti.runtime.api.connector;

import java.util.List;
import java.util.Map;
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

    public Optional<ActionDefinition> find(String implementation) {

        String connectorName = StringUtils.substringBefore(implementation,
                                                           ".");
        String actionName = StringUtils.substringAfter(implementation,
                                                       ".");

        ConnectorDefinition connectorDefinition = findConnector(connectorName);

        ActionDefinition actionDefinition = findActionDefinition(actionName,
                                                                 connectorDefinition);

        return Optional.ofNullable(actionDefinition);
    }

    private ConnectorDefinition findConnector(String connectorName) {
        List<ConnectorDefinition> resultingConnectors = connectorDefinitions.stream()
                .filter(c -> connectorName.equals(c.getName()))
                .collect(Collectors.toList());

        ConnectorDefinition connectorDefinition = null;
        if (resultingConnectors != null && !resultingConnectors.isEmpty()) {
            if (resultingConnectors.size() != 1) {
                throw new RuntimeException("Expecting exactly 1 connector definition with name mapping `" + connectorName +
                                                   "`, but were found " + resultingConnectors.size());
            }

            connectorDefinition = resultingConnectors.get(0);
        }
        return connectorDefinition;
    }

    private ActionDefinition findActionDefinition(String actionName,
                                                  ConnectorDefinition connectorDefinition) {
        ActionDefinition actionDefinition = null;
        if (connectorDefinition != null) {
            List<ActionDefinition> actionDefinitions = filterByName(connectorDefinition.getActions(),
                                                                    actionName);
            if (actionDefinitions != null) {
                if (actionDefinitions.size() != 1) {
                    throw new RuntimeException("Expecting exactly 1 action definition with name mapping `" + actionName +
                                                       "`, but were found " + actionDefinitions.size());
                }
                actionDefinition = actionDefinitions.get(0);
            }

            if (actionDefinition == null) {
                throw new RuntimeException("No action with name mapping `" + actionName + "` was found in connector `" +
                                                   connectorDefinition.getName() + "`");
            }
        }
        return actionDefinition;
    }

    private List<ActionDefinition> filterByName(Map<String, ActionDefinition> actionDefinitions,
                                                String actionName) {
        if (actionDefinitions == null || actionName == null) {
            return null;
        }
        return actionDefinitions.entrySet()
                .stream()
                .filter(entry -> actionName.equals(entry.getValue().getName()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
