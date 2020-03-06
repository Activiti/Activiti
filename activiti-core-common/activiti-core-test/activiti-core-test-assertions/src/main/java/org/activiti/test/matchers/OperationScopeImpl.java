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

package org.activiti.test.matchers;

public class OperationScopeImpl implements OperationScope {

    private String processInstanceId;

    private String  taskId;

    public OperationScopeImpl(String processInstanceId,
                              String taskId) {
        this.processInstanceId = processInstanceId;
        this.taskId = taskId;
    }

    public static OperationScope processInstanceScope(String processInstanceId) {
        return new OperationScopeImpl(processInstanceId, null);
    }

    public static OperationScope taskScope(String taskId) {
        return new OperationScopeImpl(null, taskId);
    }

    public static OperationScope scope(String processInstanceId, String taskId) {
        return new OperationScopeImpl(processInstanceId, taskId);
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

}
