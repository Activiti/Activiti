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

import java.util.List;

import org.activiti.api.process.model.ProcessInstanceMeta;

public class ProcessInstanceMetaImpl implements ProcessInstanceMeta {

    private String processInstanceId;
    private List<String> activeActivitiesIds;

    public ProcessInstanceMetaImpl() {
    }

    public ProcessInstanceMetaImpl(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public List<String> getActiveActivitiesIds() {
        return activeActivitiesIds;
    }

    public void setActiveActivitiesIds(List<String> activeActivitiesIds) {
        this.activeActivitiesIds = activeActivitiesIds;
    }
}
