/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.activiti.api.model.shared.model.IdentityLink;
import org.activiti.api.runtime.model.impl.IdentityLinkImpl;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;

public class APIIdentityLinkConverter implements ModelConverter<IdentityLinkEntity, IdentityLink> {

    @Override
    public IdentityLink from(IdentityLinkEntity internalIdentityLink) {
        if (internalIdentityLink == null) {
            return null;
        }

        IdentityLinkImpl identityLink = new IdentityLinkImpl();
        identityLink.setType(internalIdentityLink.getType());
        identityLink.setUserId(internalIdentityLink.getUserId());
        identityLink.setGroupId(internalIdentityLink.getGroupId());
        identityLink.setTaskId(internalIdentityLink.getTaskId());
        identityLink.setProcessDefinitionId(internalIdentityLink.getProcessDefinitionId());
        identityLink.setProcessInstanceId(internalIdentityLink.getProcessInstanceId());
        identityLink.setDetails(internalIdentityLink.getDetails());
        return identityLink;
    }

    @Override
    public List<IdentityLink> from(Collection<IdentityLinkEntity> internalIdentityLinks) {
        if (internalIdentityLinks == null) {
            return null;
        }
        return internalIdentityLinks.stream()
            .map(this::from)
            .collect(Collectors.toList());
    }
}
