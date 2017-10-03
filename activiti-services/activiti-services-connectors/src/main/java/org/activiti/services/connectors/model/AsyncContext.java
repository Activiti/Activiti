/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.connectors.model;

public class AsyncContext {

    private String processInstanceId;
    private String taskId;
    private String executionId;

    //used by json deserialization
    public AsyncContext() {
    }

    public AsyncContext(String processInstanceId,
                        String taskId,
                        String executionId) {
        this.processInstanceId = processInstanceId;
        this.taskId = taskId;
        this.executionId = executionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getExecutionId() {
        return executionId;
    }

}
