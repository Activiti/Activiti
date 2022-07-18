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
package org.activiti.runtime.api.model.impl;

import org.activiti.api.process.model.ProcessCandidateStarterGroup;
import org.activiti.engine.task.IdentityLink;
import org.activiti.api.runtime.model.impl.ProcessCandidateStarterGroupImpl;

public class APIProcessCandidateStarterGroupConverter extends ListConverter<IdentityLink, ProcessCandidateStarterGroup>
        implements ModelConverter<IdentityLink, ProcessCandidateStarterGroup> {

    @Override
    public ProcessCandidateStarterGroup from(IdentityLink identityLink) {
        return new ProcessCandidateStarterGroupImpl(identityLink.getProcessDefinitionId(), identityLink.getGroupId());
    }
}
