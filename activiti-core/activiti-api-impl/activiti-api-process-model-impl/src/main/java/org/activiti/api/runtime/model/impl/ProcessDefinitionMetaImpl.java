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

import org.activiti.api.process.model.ProcessDefinitionMeta;

public class ProcessDefinitionMetaImpl implements ProcessDefinitionMeta {

    private String processDefinitionKey;
    private List<String> usersIds;
    private List<String> groupIds;
    private List<String> connectorsIds;

    public ProcessDefinitionMetaImpl() {
    }

    public ProcessDefinitionMetaImpl(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    @Override
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public List<String> getUsersIds() {
        return usersIds;
    }

    public void setUsersIds(List<String> usersIds) {
        this.usersIds = usersIds;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
    }

    public List<String> getConnectorsIds() {
        return connectorsIds;
    }

    public void setConnectorsIds(List<String> connectorsIds) {
        this.connectorsIds = connectorsIds;
    }
}
