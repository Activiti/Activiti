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

package org.activiti.runtime.api.model.impl;

import org.activiti.runtime.api.model.CloudVariableInstance;
import org.activiti.runtime.api.model.VariableInstance;

public class CloudVariableInstanceImpl<T> extends CloudRuntimeEntityImpl implements CloudVariableInstance {

    private String name;
    private String type;
    private String processInstanceId;
    private String taskId;
    private T value;

    public CloudVariableInstanceImpl() {
    }

    public CloudVariableInstanceImpl(VariableInstance variableInstance) {
        name = variableInstance.getName();
        type = variableInstance.getType();
        processInstanceId = variableInstance.getProcessInstanceId();
        taskId = variableInstance.getTaskId();
        value = variableInstance.getValue();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public boolean isTaskVariable() {
        return taskId != null;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
