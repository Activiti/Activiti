/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.runtime.api.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.invocation.DefaultDelegateInterceptor;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.VariableDefinition;

public class ExpressionResolverHelper {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static void initializeExpressionResolver() {
        ProcessEngineConfigurationImpl processEngineConfiguration = mock(ProcessEngineConfigurationImpl.class);
        Context.setProcessEngineConfiguration(processEngineConfiguration);
        given(processEngineConfiguration.getExpressionManager()).willReturn(new ExpressionManager());
        given(processEngineConfiguration.getDelegateInterceptor()).willReturn(new DefaultDelegateInterceptor());
    }

    public static ExpressionResolver initContext(DelegateExecution execution,
                                                Extension extensions) {
        initializeExpressionResolver();

        Map<String, Object> variables = convertToStringObjectMap(extensions.getProperties());

        setExecutionVariables(execution, variables);
        return new ExpressionResolver(new ExpressionManager(),
                                      objectMapper, new DefaultDelegateInterceptor());
    }

    public static void setExecutionVariables(DelegateExecution execution, Map<String, Object> variables) {
        given(execution.getVariables()).willReturn(variables);
        given(execution.getVariablesLocal()).willReturn(variables);
        for (String key : variables.keySet()) {
            given(execution.hasVariable(key)).willReturn(true);
            VariableInstance var = getVariableInstance(key,
                                                       variables.get(key));
            given(execution.getVariableInstance(key)).willReturn(var);
            given(execution.getVariable(key)).willReturn(variables.get(key));
        }
    }


    private static Map<String, Object> convertToStringObjectMap(
        Map<String, VariableDefinition> sourceMap) {
        Map<String, Object> result = new HashMap<>();
        sourceMap.forEach((key,
                value) -> result.put(value.getName(),
                                     value.getValue()));
        return result;
    }

    private static VariableInstance getVariableInstance(String key,
                                                        Object value) {
        VariableInstance var = new VariableInstance() {
            @Override
            public void setRevision(int revision) {
            }

            @Override
            public int getRevisionNext() {
                return 0;
            }

            @Override
            public int getRevision() {
                return 0;
            }

            @Override
            public void setUpdated(boolean updated) {
            }

            @Override
            public void setInserted(boolean inserted) {
            }

            @Override
            public void setId(String id) {
            }

            @Override
            public void setDeleted(boolean deleted) {
            }

            @Override
            public boolean isUpdated() {
                return false;
            }

            @Override
            public boolean isInserted() {
                return false;
            }

            @Override
            public boolean isDeleted() {
                return false;
            }

            @Override
            public Object getPersistentState() {
                return null;
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public void setTextValue2(String textValue2) {
            }

            @Override
            public void setTextValue(String textValue) {
            }

            @Override
            public void setLongValue(Long longValue) {
            }

            @Override
            public void setDoubleValue(Double doubleValue) {
            }

            @Override
            public void setCachedValue(Object cachedValue) {
            }

            @Override
            public void setBytes(byte[] bytes) {
            }

            @Override
            public String getTextValue2() {
                return null;
            }

            @Override
            public String getTextValue() {
                return null;
            }

            @Override
            public String getName() {

                return null;
            }

            @Override
            public Long getLongValue() {
                return null;
            }

            @Override
            public Double getDoubleValue() {
                return null;
            }

            @Override
            public Object getCachedValue() {
                return null;
            }

            @Override
            public byte[] getBytes() {
                return null;
            }

            @Override
            public void setValue(Object value) {
            }

            @Override
            public void setTypeName(String typeName) {
            }

            @Override
            public void setTaskId(String taskId) {
            }

            @Override
            public void setProcessInstanceId(String processInstanceId) {
            }

            @Override
            public void setName(String name) {
            }

            @Override
            public void setExecutionId(String executionId) {
            }

            @Override
            public Object getValue() {
                return value;
            }

            @Override
            public String getTypeName() {
                if (value instanceof String) {
                    return "string";
                } else if (value instanceof Integer) {
                    return "integer";
                } else if (value instanceof Boolean) {
                    return "boolean";
                } else {
                    return "json";
                }
            }

            @Override
            public String getTaskId() {
                return null;
            }

            @Override
            public String getProcessInstanceId() {
                return null;
            }

            @Override
            public String getExecutionId() {
                return null;
            }
        };
        return var;
    }
}
