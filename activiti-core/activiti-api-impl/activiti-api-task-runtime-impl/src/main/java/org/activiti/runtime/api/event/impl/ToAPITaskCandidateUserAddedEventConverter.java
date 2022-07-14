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
package org.activiti.runtime.api.event.impl;

import org.activiti.api.task.runtime.events.TaskCandidateUserAddedEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.task.IdentityLink;
import org.activiti.runtime.api.model.impl.APITaskCandidateUserConverter;

import java.util.Optional;

public class ToAPITaskCandidateUserAddedEventConverter implements EventConverter<TaskCandidateUserAddedEvent, ActivitiEntityEvent> {

    private APITaskCandidateUserConverter converter;
    private TaskCandidateEventConverterHelper taskCandidateEventConverterHelper = new TaskCandidateEventConverterHelper();

    public ToAPITaskCandidateUserAddedEventConverter(APITaskCandidateUserConverter converter) {
        this.converter = converter;
    }

    @Override
    public Optional<TaskCandidateUserAddedEvent> from(ActivitiEntityEvent internalEvent) {
        TaskCandidateUserAddedEventImpl event = null;
        if (internalEvent.getEntity() instanceof IdentityLink) {
            IdentityLink identityLink = (IdentityLink) internalEvent.getEntity();
            if (taskCandidateEventConverterHelper.isTaskCandidateUserLink(identityLink)) {
                event = new TaskCandidateUserAddedEventImpl(converter.from(identityLink));
            }
        }
        return Optional.ofNullable(event);
    }

}
