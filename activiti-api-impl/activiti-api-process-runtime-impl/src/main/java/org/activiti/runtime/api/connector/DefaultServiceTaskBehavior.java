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
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultServiceTaskBehavior extends AbstractBpmnActivityBehavior {

    private final ApplicationContext applicationContext;
    private final IntegrationContextBuilder integrationContextBuilder;
    private final List<ConnectorDefinition> connectorDefinitions;
    private final ConnectorActionDefinitionFinder connectorActionDefinitionFinder;
    private final VariablesMatchHelper variablesMatchHelper;

    public DefaultServiceTaskBehavior(ApplicationContext applicationContext,
                                      IntegrationContextBuilder integrationContextBuilder, List<ConnectorDefinition> connectorDefinitions, ConnectorActionDefinitionFinder connectorActionDefinitionFinder, VariablesMatchHelper variablesMatchHelper) {
        this.applicationContext = applicationContext;
        this.integrationContextBuilder = integrationContextBuilder;
        this.connectorDefinitions = connectorDefinitions;
        this.connectorActionDefinitionFinder = connectorActionDefinitionFinder;
        this.variablesMatchHelper = variablesMatchHelper;
    }

    /**
     *
     * We have two different implementation strategy that can be executed
     * in according if we have a connector action definition match or not.
     *
     **/
    @Override
    public void execute(DelegateExecution execution) {
        Connector connector;
        IntegrationContext context;

        String implementation = ((ServiceTask) execution.getCurrentFlowElement()).getImplementation();

        Optional<ActionDefinition> actionDefinitionOptional = connectorActionDefinitionFinder.find(implementation, connectorDefinitions);
        ActionDefinition actionDefinition = null;
        if(actionDefinitionOptional.isPresent()){
            actionDefinition = actionDefinitionOptional.get();
            context = integrationContextBuilder.from(execution, actionDefinition);
            connector = applicationContext.getBean(actionDefinition.getName(), Connector.class);
        }else{
            context = integrationContextBuilder.from(execution, null);
            connector = applicationContext.getBean(implementation,
                    Connector.class);
        }

        IntegrationContext results = connector.execute(context);

        List<VariableDefinition> outBoundVariableDefinitions = actionDefinition==null?null:actionDefinition.getOutput();

        execution.setVariables(variablesMatchHelper.match(results.getOutBoundVariables(), outBoundVariableDefinitions));

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
