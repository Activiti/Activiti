/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.persistence.entity.VariableInstance;

public class OutputMappingVariableScope extends NoExecutionVariableScope {

    private static final String VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG = "No execution active, variable instance can't be modified";
    private Map<String, Object> variables;
    private Map<String, VariableInstance> instances = new HashMap<>();

    public OutputMappingVariableScope() {
        super();
        variables = Collections.emptyMap();
    }

    public OutputMappingVariableScope(Map<String, Object> variables) {
        super();
        this.variables = variables;
        variables.forEach((k, v) -> instances.put(k, getVariableInstance(k, v)));
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public Map<String, Object> getVariablesLocal() {
        return getVariables();
    }

    @Override
    public Map<String, Object> getVariables(Collection<String> variableNames) {
        Map<String, Object> variablesRequested = new HashMap<>();
        variableNames.forEach(key -> {
            if (variables.containsKey(key)) {
                variablesRequested.put(key, variables.get(key));
            }
        });
        return variablesRequested;
    }

    @Override
    public Map<String, Object> getVariables(Collection<String> variableNames, boolean fetchAllVariables) {
        return getVariables(variableNames);
    }

    @Override
    public Map<String, Object> getVariablesLocal(Collection<String> variableNames) {
        return getVariables(variableNames, true);
    }

    @Override
    public Map<String, Object> getVariablesLocal(Collection<String> variableNames, boolean fetchAllVariables) {
        return getVariables(variableNames, fetchAllVariables);
    }

    @Override
    public Object getVariable(String variableName) {
        return variables.get(variableName);
    }

    @Override
    public Object getVariable(String variableName, boolean fetchAllVariables) {
        return variables.get(variableName);
    }

    @Override
    public Object getVariableLocal(String variableName) {
        return variables.get(variableName);
    }

    @Override
    public Object getVariableLocal(String variableName, boolean fetchAllVariables) {
        return variables.get(variableName);
    }

    @Override
    public <T> T getVariable(String variableName, Class<T> variableClass) {
        return variableClass.cast(getVariable(variableName));
    }

    @Override
    public <T> T getVariableLocal(String variableName, Class<T> variableClass) {
        return variableClass.cast(getVariable(variableName));
    }

    @Override
    public Set<String> getVariableNames() {
        return variables.keySet();
    }

    @Override
    public Set<String> getVariableNamesLocal() {
        return variables.keySet();
    }

    @Override
    public Map<String, VariableInstance> getVariableInstances() {
        return instances;
    }

    @Override
    public Map<String, VariableInstance> getVariableInstances(Collection<String> variableNames) {
        Map<String, VariableInstance> variablesRequested = new HashMap<>();
        variableNames.forEach(key -> {
            if (instances.containsKey(key)) {
                variablesRequested.put(key, instances.get(key));
            }
        });
        return variablesRequested;
    }

    @Override
    public Map<String, VariableInstance> getVariableInstances(Collection<String> variableNames,
                                                              boolean fetchAllVariables) {
        return getVariableInstances(variableNames);
    }

    @Override
    public Map<String, VariableInstance> getVariableInstancesLocal() {
        return getVariableInstances();
    }

    @Override
    public Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> variableNames) {
        return getVariableInstances(variableNames);
    }

    @Override
    public Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> variableNames,
                                                                   boolean fetchAllVariables) {
        return getVariableInstances(variableNames, fetchAllVariables);
    }

    @Override
    public VariableInstance getVariableInstance(String variableName) {
        return instances.get(variableName);
    }

    @Override
    public VariableInstance getVariableInstance(String variableName, boolean fetchAllVariables) {
        return getVariableInstance(variableName);
    }

    @Override
    public VariableInstance getVariableInstanceLocal(String variableName) {
        return getVariableInstance(variableName);
    }

    @Override
    public VariableInstance getVariableInstanceLocal(String variableName, boolean fetchAllVariables) {
        return getVariableInstance(variableName, fetchAllVariables);
    }

    @Override
    public boolean hasVariables() {
        return !variables.isEmpty();
    }

    @Override
    public boolean hasVariablesLocal() {
        return !variables.isEmpty();
    }

    @Override
    public boolean hasVariable(String variableName) {
        return variables.containsKey(variableName);
    }

    @Override
    public boolean hasVariableLocal(String variableName) {
        return variables.containsKey(variableName);
    }

    private VariableInstance getVariableInstance(String key,
                                                 Object value) {
        return new VariableInstance() {

            @Override
            public void setRevision(int revision) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
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
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setInserted(boolean inserted) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setId(String id) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setDeleted(boolean deleted) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public boolean isUpdated() {
                return true;
            }

            @Override
            public boolean isInserted() {
                return true;
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
                return key;
            }

            @Override
            public void setTextValue2(String textValue2) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setTextValue(String textValue) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setLongValue(Long longValue) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setDoubleValue(Double doubleValue) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setCachedValue(Object cachedValue) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setBytes(byte[] bytes) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public String getTextValue2() {
                return value.toString();
            }

            @Override
            public String getTextValue() {
                return value.toString();
            }

            @Override
            public String getName() {
                return key;
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
                return value;
            }

            @Override
            public byte[] getBytes() {
                return value.toString().getBytes();
            }

            @Override
            public void setValue(Object value) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setTypeName(String typeName) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setTaskId(String taskId) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setProcessInstanceId(String processInstanceId) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setName(String name) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
            }

            @Override
            public void setExecutionId(String executionId) {
                throw new UnsupportedOperationException(VARIABLE_INSTANCE_MODIFICATION_ERROR_MSG);
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
    }

}
