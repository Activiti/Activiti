/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.connector;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.model.connector.ActionDefinition;
import org.activiti.model.connector.ConnectorDefinition;
import org.activiti.model.connector.VariableDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultServiceTaskBehavior extends AbstractBpmnActivityBehavior {

    private final ApplicationContext applicationContext;
    private final IntegrationContextBuilder integrationContextBuilder;
    private final List<ConnectorDefinition> connectorDefinitions;

    public DefaultServiceTaskBehavior(ApplicationContext applicationContext,
                                      IntegrationContextBuilder integrationContextBuilder, List<ConnectorDefinition> connectorDefinitions) {
        this.applicationContext = applicationContext;
        this.integrationContextBuilder = integrationContextBuilder;
        this.connectorDefinitions = connectorDefinitions;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Connector connector;
        IntegrationContext context;

        String implementation = ((ServiceTask) execution.getCurrentFlowElement()).getImplementation();
        String connectorId = StringUtils.substringBefore(implementation, ".");
        String actionId = StringUtils.substringAfter(implementation, ".");

        boolean hasMatchingConnectorDefinitions = false;
        ActionDefinition actionDefinition = null;

        List<ConnectorDefinition> resultingConnectors = connectorDefinitions.stream().filter(c -> c.getId().equals(connectorId)).collect(Collectors.toList());
        if (resultingConnectors == null || resultingConnectors.size() == 0) {
            context = integrationContextBuilder.from(execution, null);
            connector = applicationContext.getBean(implementation,
                    Connector.class);
        } else {
            hasMatchingConnectorDefinitions = true;
            if (resultingConnectors.size() != 1) {
                throw new RuntimeException("Mismatch connector id mapping: " + connectorId);
            }
            ConnectorDefinition connectorDefinition = resultingConnectors.get(0);

            actionDefinition = connectorDefinition.getActions().get(actionId);
            if (actionDefinition == null) {
                throw new RuntimeException("Mismatch action id mapping: " + actionId);
            }
            context = integrationContextBuilder.from(execution, actionDefinition);
            connector = applicationContext.getBean(actionDefinition.getName(), Connector.class);
        }

        IntegrationContext results = connector.execute(context);

        Map<String, Object> outBoundVariables = new HashMap<>();
        if (!hasMatchingConnectorDefinitions) {
            outBoundVariables = results.getOutBoundVariables();
        } else {
            for (VariableDefinition variableDefinition : actionDefinition.getOutput()) {
                Object outBoundVariableValue = results.getOutBoundVariables().get(variableDefinition.getName());
                if (outBoundVariableValue != null) {
                    outBoundVariables.put(variableDefinition.getName(), outBoundVariableValue);
                }
            }
        }

        execution.setVariables(outBoundVariables);

        leave(execution);
    }

    private String getServiceTaskImplementation(DelegateExecution execution) {
        return ((ServiceTask) execution.getCurrentFlowElement()).getImplementation();
    }

    protected boolean hasConnectorBean(DelegateExecution execution) {
        String implementation = getServiceTaskImplementation(execution);
        return applicationContext.containsBean(implementation)
                && applicationContext.getBean(implementation) instanceof Connector;
    }

}
