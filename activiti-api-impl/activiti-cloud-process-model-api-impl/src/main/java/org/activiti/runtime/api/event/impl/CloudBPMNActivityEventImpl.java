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

package org.activiti.runtime.api.event.impl;

import org.activiti.runtime.api.event.BPMNActivityEvent;
import org.activiti.runtime.api.model.BPMNActivity;

public abstract class CloudBPMNActivityEventImpl extends CloudRuntimeEventImpl<BPMNActivity, BPMNActivityEvent.ActivityEvents> {

    private String processDefinitionId;
    private String processInstanceId;

    public CloudBPMNActivityEventImpl() {
    }

    public CloudBPMNActivityEventImpl(BPMNActivity entity,
                                      String processDefinitionId,
                                      String processInstanceId) {
        super(entity);
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
        setEntityId(processInstanceId);
    }

    public CloudBPMNActivityEventImpl(String id,
                                      Long timestamp,
                                      BPMNActivity entity,
                                      String processDefinitionId,
                                      String processInstanceId) {
        super(id,
              timestamp,
              entity);
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
        setEntityId(processInstanceId);
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        setEntityId(processInstanceId);
        this.processInstanceId = processInstanceId;
    }
}
