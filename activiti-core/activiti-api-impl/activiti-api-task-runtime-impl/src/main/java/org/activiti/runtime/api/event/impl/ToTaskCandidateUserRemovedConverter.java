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
import org.activiti.api.task.runtime.events.TaskCandidateUserRemovedEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.task.IdentityLink;
import org.activiti.runtime.api.model.impl.APITaskCandidateUserConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToTaskCandidateUserRemovedConverter
    implements EventConverter<TaskCandidateUserRemovedEvent, ActivitiEntityEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ToTaskCandidateUserRemovedConverter.class);

    private APITaskCandidateUserConverter converter;
    private TaskCandidateEventConverterHelper taskCandidateEventConverterHelper =
        new TaskCandidateEventConverterHelper();

    public ToTaskCandidateUserRemovedConverter(APITaskCandidateUserConverter converter) {
        this.converter = converter;
    }

    @Override
    public Optional<TaskCandidateUserRemovedEvent> from(ActivitiEntityEvent internalEvent) {
        TaskCandidateUserRemovedImpl event = null;
        if (internalEvent.getEntity() instanceof IdentityLink) {
            IdentityLink entity = (IdentityLink) internalEvent.getEntity();
            if (taskCandidateEventConverterHelper.isTaskCandidateUserLink(entity)) {
                event = new TaskCandidateUserRemovedImpl(converter.from(entity));
                event.setProcessInstanceId(internalEvent.getProcessInstanceId());
                event.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
                logger.debug(
                    "TaskCandidateUserRemoved converted: taskId={}, userId={}, link.pid={}, link.pdefId={}, event.pid={}, event.pdefId={}",
                    entity.getTaskId(),
                    entity.getUserId(),
                    entity.getProcessInstanceId(),
                    entity.getProcessDefinitionId(),
                    event.getProcessInstanceId(),
                    event.getProcessDefinitionId()
                );
            }
        }
        return Optional.ofNullable(event);
    }
}
