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
import org.activiti.runtime.api.impl.VariablesMappingProvider;
import org.springframework.context.ApplicationContext;

import static org.activiti.runtime.api.impl.MappingExecutionContext.buildMappingExecutionContext;

public class DefaultServiceTaskBehavior extends AbstractBpmnActivityBehavior {

    private final ApplicationContext applicationContext;
    private final IntegrationContextBuilder integrationContextBuilder;
    private VariablesMappingProvider outboundVariablesProvider;

    public DefaultServiceTaskBehavior(ApplicationContext applicationContext,
                                      IntegrationContextBuilder integrationContextBuilder,
                                      VariablesMappingProvider outboundVariablesProvider) {
        this.applicationContext = applicationContext;
        this.integrationContextBuilder = integrationContextBuilder;
        this.outboundVariablesProvider = outboundVariablesProvider;
    }

    /**
     * We have two different implementation strategy that can be executed
     * in according if we have a connector action definition match or not.
     **/
    @Override
    public void execute(DelegateExecution execution) {
        Connector connector = getConnector(getImplementation(execution));
        IntegrationContext integrationContext = connector.apply(integrationContextBuilder.from(execution));

        execution.setVariables(outboundVariablesProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                                                                                  integrationContext.getOutBoundVariables()));

        leave(execution);
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
        return applicationContext.containsBean(implementation) && applicationContext.getBean(implementation) instanceof Connector;
    }
}
