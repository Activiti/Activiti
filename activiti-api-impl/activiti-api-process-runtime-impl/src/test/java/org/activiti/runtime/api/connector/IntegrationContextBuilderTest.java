///*
// * Copyright 2018 Alfresco, Inc. and/or its affiliates.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.activiti.runtime.api.connector;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.activiti.api.process.model.IntegrationContext;
//import org.activiti.engine.delegate.DelegateExecution;
//import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntity;
//import org.activiti.model.connector.ActionDefinition;
//import org.activiti.model.connector.ConnectorDefinition;
//import org.activiti.model.connector.VariableDefinition;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.InjectMocks;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.*;
//import static org.mockito.MockitoAnnotations.initMocks;
//
//public class IntegrationContextBuilderTest {
//
//    @InjectMocks
//    private IntegrationContextBuilder integrationContextBuilder;
//
//    private static final String ACTION_ID = "action1";
//
//    @Before
//    public void setUp() {
//        initMocks(this);
//    }
//
//    @Test
//    public void inBoundVariablesSetFromDelegateExecutionBecauseConnectorsAreNotSet() {
//        Map<String, Object> vars = new HashMap<>();
//        vars.put("var1",
//                 "value");
//        given(delegateExecution.getVariables()).willReturn(vars);
//
//        IntegrationContext integrationContext = integrationContextBuilder.from(buildIntegrationContextEntity(),
//                                                                               delegateExecution,
//                                                                               null);
//        assertThat(integrationContext.getInBoundVariables()).isEqualTo(vars);
//    }
//
//    /**
//     * 2 input variable coming from the connector definition for a given action,
//     * but one variable name matches the variables present in the delegate execution.
//     * So only the one matching is set.
//     *
//     **/
//    @Test
//    public void inBoundVariablesSetFromConnectorActionInputVarsThatMatchVariablesInDelegateExecution() {
//        String connectorId = CONNECTOR_ID;
//        String actionId = ACTION_ID;
//        DelegateExecution delegateExecution = ConnectorRuntimeApiTestHelper.buildExecution(connectorId + "." + actionId);
//        Map<String, Object> vars = new HashMap<>();
//        vars.put("value",
//                 "value");
//        given(delegateExecution.getVariables()).willReturn(vars);
//
//        List<ConnectorDefinition> connectors = buildConnectors(connectorId,
//                                                     actionId);
//
//        IntegrationContext integrationContext = integrationContextBuilder.from(buildIntegrationContextEntity(),
//                                                                               delegateExecution,
//                                                                               actionDefinition);
//        assertThat(integrationContext.getInBoundVariables()).hasSize(1);
//        Variable resultingVariable = (Variable) integrationContext.getInBoundVariables().get("value");
//        assertThat(resultingVariable.getName()).isEqualTo("value");
//    }
//
//    private List<Connector> buildConnectors(String connectorId,
//                                            String actionId) {
//        List<Connector> connectors = new ArrayList<>();
//        Connector connector = new Connector();
//        connector.setId(connectorId);
//        Map<String, Action> actions = new HashMap<>();
//        Action action = new Action();
//        action.setId(actionId);
//        List<Variable> variables = new ArrayList<>();
//        Variable matchingVariable = new Variable();
//        matchingVariable.setName("value");
//        Variable notMatchingVariable = new Variable();
//        notMatchingVariable.setName("does_not_match");
//        variables.add(matchingVariable);
//        variables.add(notMatchingVariable);
//        action.setInput(variables);
//        actions.put(actionId,
//                    action);
//        connector.setActions(actions);
//        connectors.add(connector);
//        return connectors;
//    }
//
//    private IntegrationContextEntity buildIntegrationContextEntity() {
//        IntegrationContextEntity integrationContextEntity = mock(IntegrationContextEntity.class);
//        given(integrationContextEntity.getId()).willReturn("id");
//        return integrationContextEntity;
//    }
//}
