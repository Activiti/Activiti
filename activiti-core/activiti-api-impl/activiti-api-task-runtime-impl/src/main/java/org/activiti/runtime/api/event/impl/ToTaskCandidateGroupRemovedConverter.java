/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.runtime.api.event.impl;

import java.util.Optional;
import org.activiti.api.task.runtime.events.TaskCandidateGroupRemovedEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.task.IdentityLink;
import org.activiti.runtime.api.model.impl.APITaskCandidateGroupConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToTaskCandidateGroupRemovedConverter
    implements EventConverter<TaskCandidateGroupRemovedEvent, ActivitiEntityEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ToTaskCandidateGroupRemovedConverter.class);

    private APITaskCandidateGroupConverter converter;
    private TaskCandidateEventConverterHelper taskCandidateEventConverterHelper =
        new TaskCandidateEventConverterHelper();

    public ToTaskCandidateGroupRemovedConverter(APITaskCandidateGroupConverter converter) {
        this.converter = converter;
    }

    @Override
    public Optional<TaskCandidateGroupRemovedEvent> from(ActivitiEntityEvent internalEvent) {
        TaskCandidateGroupRemovedImpl event = null;
        if (internalEvent.getEntity() instanceof IdentityLink) {
            IdentityLink identityLink = (IdentityLink) internalEvent.getEntity();
            if (taskCandidateEventConverterHelper.isTaskCandidateGroupLink(identityLink)) {
                event = new TaskCandidateGroupRemovedImpl(converter.from(identityLink));
                event.setProcessInstanceId(internalEvent.getProcessInstanceId());
                event.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
                logger.debug(
                    "TaskCandidateGroupRemoved converted: taskId={}, groupId={}, link.pid={}, link.pdefId={}, event.pid={}, event.pdefId={}",
                    identityLink.getTaskId(),
                    identityLink.getGroupId(),
                    identityLink.getProcessInstanceId(),
                    identityLink.getProcessDefinitionId(),
                    event.getProcessInstanceId(),
                    event.getProcessDefinitionId()
                );
            }
        }
        return Optional.ofNullable(event);
    }
}
