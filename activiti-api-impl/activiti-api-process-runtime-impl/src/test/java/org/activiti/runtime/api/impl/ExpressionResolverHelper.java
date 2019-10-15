package org.activiti.runtime.api.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.invocation.DefaultDelegateInterceptor;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.VariableInstance;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ExpressionResolverHelper {

    private static void initializeExpressionResolver() {
        ProcessEngineConfigurationImpl processEngineConfiguration = mock(ProcessEngineConfigurationImpl.class);
        Context.setProcessEngineConfiguration(processEngineConfiguration);
        given(processEngineConfiguration.getExpressionManager()).willReturn(new ExpressionManager());
        given(processEngineConfiguration.getDelegateInterceptor()).willReturn(new DefaultDelegateInterceptor());
    }

    public static DelegateExecution initContext(DelegateExecution execution,
                                                Map<String, Object> variables)
            throws JsonParseException, JsonMappingException, IOException {
        initializeExpressionResolver();

        given(execution.getVariables()).willReturn(variables);
        given(execution.getVariablesLocal()).willReturn(variables);
        for (String key : variables.keySet()) {
            given(execution.hasVariable(key)).willReturn(true);
            VariableInstance var = getVariableInstance(key,
                                                       variables.get(key));
            given(execution.getVariableInstance(key)).willReturn(var);
        }
        return execution;
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
