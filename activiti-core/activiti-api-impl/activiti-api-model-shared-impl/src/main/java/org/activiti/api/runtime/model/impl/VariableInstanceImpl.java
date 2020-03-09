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

package org.activiti.api.runtime.model.impl;

import org.activiti.api.model.shared.model.VariableInstance;

public class VariableInstanceImpl<T> implements VariableInstance {

    private String name;
    private String type;
    private String processInstanceId;
    private T value;
    private String taskId;

    public VariableInstanceImpl() {
    }

    public VariableInstanceImpl(String name,
                                String type,
                                T value,
                                String processInstanceId) {
        this.name = name;
        this.type = type;
        this.processInstanceId = processInstanceId;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
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

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "VariableInstanceImpl{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", processInstanceId='" + processInstanceId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", value='" + value.toString() + '\'' +
                '}';
    }

}
