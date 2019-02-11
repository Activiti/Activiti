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

import java.util.Optional;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.springframework.context.ApplicationContext;

public class DefaultServiceTaskBehavior extends AbstractBpmnActivityBehavior {

    private final ApplicationContext applicationContext;
    private final IntegrationContextBuilder integrationContextBuilder;
    private ConnectorActionDefinitionFinder connectorActionDefinitionFinder;
    private OutboundVariablesProvider outboundVariablesProvider;

    public DefaultServiceTaskBehavior(ApplicationContext applicationContext,
                                      IntegrationContextBuilder integrationContextBuilder,
                                      ConnectorActionDefinitionFinder connectorActionDefinitionFinder,
                                      OutboundVariablesProvider outboundVariablesProvider) {
        this.applicationContext = applicationContext;
        this.connectorActionDefinitionFinder = connectorActionDefinitionFinder;
        this.integrationContextBuilder = integrationContextBuilder;
        this.outboundVariablesProvider = outboundVariablesProvider;
    }

    /**
     * We have two different implementation strategy that can be executed
     * in according if we have a connector action definition match or not.
     **/
    @Override
    public void execute(DelegateExecution execution) {
        ActionDefinition actionDefinition = findRelatedActionDefinition(execution);

        Connector connector = getConnector(getImplementation(execution));
        IntegrationContext integrationContext = connector.apply(integrationContextBuilder.from(execution,
                                                                                    actionDefinition));

        execution.setVariables(outboundVariablesProvider.calculateVariables(integrationContext,
                                                                            actionDefinition));

        leave(execution);
    }

    public ActionDefinition findRelatedActionDefinition(DelegateExecution execution) {
        String implementation = getImplementation(execution);
        Optional<ActionDefinition> actionDefinitionOptional = connectorActionDefinitionFinder.find(implementation);
        return actionDefinitionOptional.orElse(null);
    }

    private String getImplementation(DelegateExecution execution) {
        return ((ServiceTask) execution.getCurrentFlowElement()).getImplementation();
    }

    private Connector getConnector(String implementation) {
        return applicationContext.getBean(implementation,
                                          Connector.class);
    }

    private String getServiceTaskImplementation(DelegateExecution execution) {
        return ((ServiceTask) execution.getCurrentFlowElement()).getImplementation();
    }

    public boolean hasConnectorBean(DelegateExecution execution) {
        String implementation = getServiceTaskImplementation(execution);
        return applicationContext.containsBean(implementation)
                && applicationContext.getBean(implementation) instanceof Connector;
    }
}
