/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.api.runtime.model.impl;

import org.activiti.api.process.model.ProcessCandidateStarterGroup;

import java.util.Objects;

public class ProcessCandidateStarterGroupImpl extends ProcessCandidateStarterImpl implements ProcessCandidateStarterGroup {

    private String groupId;

    public ProcessCandidateStarterGroupImpl() {
    }

    public ProcessCandidateStarterGroupImpl(String processDefinitionId, String groupId) {
        super(processDefinitionId);
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessCandidateStarterGroupImpl that = (ProcessCandidateStarterGroupImpl) o;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(getProcessDefinitionId(), that.getProcessDefinitionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProcessDefinitionId(), groupId);
    }

    @Override
    public String getGroupId() {
        return this.groupId;
    }
}
