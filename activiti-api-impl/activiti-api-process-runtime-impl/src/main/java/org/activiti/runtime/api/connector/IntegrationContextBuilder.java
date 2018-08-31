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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntity;
import org.activiti.model.connector.Action;
import org.activiti.model.connector.Connector;
import org.activiti.model.connector.Variable;
import org.activiti.runtime.api.model.impl.IntegrationContextImpl;
import org.apache.commons.lang3.StringUtils;

public class IntegrationContextBuilder {

    public IntegrationContext from(IntegrationContextEntity integrationContextEntity,
                                   DelegateExecution execution,
                                   List<Connector> connectors) {
        IntegrationContextImpl integrationContext = buildFromExecution(execution,
                                                                       connectors);
        integrationContext.setId(integrationContextEntity.getId());
        return integrationContext;
    }

    public IntegrationContext from(DelegateExecution execution) {
        IntegrationContextImpl integrationContext = buildFromExecution(execution,
                                                                       null);
        return integrationContext;
    }

    private IntegrationContextImpl buildFromExecution(DelegateExecution execution,
                                                      List<Connector> connectors) {
        IntegrationContextImpl integrationContext = new IntegrationContextImpl();
        integrationContext.setProcessInstanceId(execution.getProcessInstanceId());
        integrationContext.setProcessDefinitionId(execution.getProcessDefinitionId());
        integrationContext.setActivityElementId(execution.getCurrentActivityId());

        String implementation = ((ServiceTask) execution.getCurrentFlowElement()).getImplementation();

        integrationContext.setConnectorType(implementation);

        integrationContext.setInBoundVariables(buildInBoundVariables(implementation,
                                                                     connectors,
                                                                     execution));

        return integrationContext;
    }

    private Map<String, Object> buildInBoundVariables(String implementation,
                                                      List<Connector> connectors,
                                                      DelegateExecution execution) {
        Map<String, Object> inBoundVariables;
        if (connectors != null && !connectors.isEmpty()) {
            String connectorName = StringUtils.substringBefore(implementation,
                                                               ".");
            String actionName = StringUtils.substringAfter(implementation,
                                                           ".");

            List<Connector> resultingConnectors = connectors.stream().filter(connector -> connector.getName().equals(connectorName)).collect(Collectors.toList());
            if (resultingConnectors.size() != 1) {
                throw new RuntimeException("Mismatch connector name mapping");
            }
            Connector connector = resultingConnectors.get(0);
            Action action = connector.getActions().get(actionName);
            if (action == null) {
                throw new RuntimeException("Mismatch action name mapping");
            }
            inBoundVariables = action.getInput().stream().collect(Collectors.toMap(Variable::getName,
                                                                                   Function.identity()));
        } else {
            inBoundVariables = execution.getVariables();
        }
        return inBoundVariables;
    }
}
