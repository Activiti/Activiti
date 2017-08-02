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

package org.activiti.services.audit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.activiti.services.core.model.events.ProcessEngineEvent;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockProcessEngineEvent implements ProcessEngineEvent {

    private Long timestamp;
    private String eventType;
    private String executionId;
    private String processDefinitionId;
    private String processInstanceId;

    public MockProcessEngineEvent() {
    }

    public MockProcessEngineEvent(Long timestamp,
                                  String eventType,
                                  String executionId,
                                  String processDefinitionId,
                                  String processInstanceId) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.executionId = executionId;
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public String getExecutionId() {
        return executionId;
    }

    @Override
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }
}
