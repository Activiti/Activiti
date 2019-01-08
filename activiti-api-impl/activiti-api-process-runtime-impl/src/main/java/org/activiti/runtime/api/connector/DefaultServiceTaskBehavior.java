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
import java.util.Optional;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.activiti.core.common.model.connector.VariableDefinition;
import org.springframework.context.ApplicationContext;

public class DefaultServiceTaskBehavior extends AbstractBpmnActivityBehavior {

    private final ApplicationContext applicationContext;
    private final IntegrationContextBuilder integrationContextBuilder;
    private ConnectorActionDefinitionFinder connectorActionDefinitionFinder;
    private VariablesMatchHelper variablesMatchHelper;

    public DefaultServiceTaskBehavior(ApplicationContext applicationContext,
                                      IntegrationContextBuilder integrationContextBuilder,
                                      ConnectorActionDefinitionFinder connectorActionDefinitionFinder,
                                      VariablesMatchHelper variablesMatchHelper) {
        this.applicationContext = applicationContext;
        this.integrationContextBuilder = integrationContextBuilder;
        this.connectorActionDefinitionFinder = connectorActionDefinitionFinder;
        this.variablesMatchHelper = variablesMatchHelper;
    }

    /**
     * We have two different implementation strategy that can be executed
     * in according if we have a connector action definition match or not.
     **/
    @Override
    public void execute(DelegateExecution execution) {
        Connector connector;
        IntegrationContext context;

        String implementation = ((ServiceTask) execution.getCurrentFlowElement()).getImplementation();
        List<VariableDefinition> outBoundVariableDefinitions = null;
        if(connectorActionDefinitionFinder != null) {

            Optional<ActionDefinition> actionDefinitionOptional = connectorActionDefinitionFinder.find(implementation);
            ActionDefinition actionDefinition = null;
            if (actionDefinitionOptional.isPresent()) {
                actionDefinition = actionDefinitionOptional.get();
                context = integrationContextBuilder.from(execution,
                        actionDefinition);
                connector = applicationContext.getBean(actionDefinition.getName(),
                        Connector.class);
            } else {
                context = integrationContextBuilder.from(execution,
                        null);
                connector = applicationContext.getBean(implementation,
                        Connector.class);
            }
            outBoundVariableDefinitions = actionDefinition == null ? null : actionDefinition.getOutputs();
        }else {
            context = integrationContextBuilder.from(execution,
                            null);
            connector = applicationContext.getBean(implementation,
                            Connector.class);
        }

        IntegrationContext results = connector.execute(context);

        if(variablesMatchHelper != null) {
            execution.setVariables(variablesMatchHelper.match(results.getOutBoundVariables(),
                    outBoundVariableDefinitions));
        }else{
            execution.setVariables(results.getOutBoundVariables());
        }

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
