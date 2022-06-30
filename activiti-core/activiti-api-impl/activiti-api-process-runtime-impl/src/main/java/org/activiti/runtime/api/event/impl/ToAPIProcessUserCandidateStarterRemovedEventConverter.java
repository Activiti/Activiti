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

import org.activiti.api.process.runtime.events.ProcessUserCandidateStarterRemovedEvent;
import org.activiti.api.runtime.event.impl.ProcessUserCandidateStarterRemovedEventImpl;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.task.IdentityLink;
import org.activiti.runtime.api.model.impl.APIProcessUserCandidateStarterConverter;

import java.util.Optional;

public class ToAPIProcessUserCandidateStarterRemovedEventConverter implements EventConverter<ProcessUserCandidateStarterRemovedEvent, ActivitiEntityEvent> {

    private APIProcessUserCandidateStarterConverter converter;
    private ProcessCandidateStarterEventConverterHelper processCandidateStarterEventConverterHelper = new ProcessCandidateStarterEventConverterHelper();

    public ToAPIProcessUserCandidateStarterRemovedEventConverter(APIProcessUserCandidateStarterConverter converter) {
        this.converter = converter;
    }

    @Override
    public Optional<ProcessUserCandidateStarterRemovedEvent> from(ActivitiEntityEvent internalEvent) {
        ProcessUserCandidateStarterRemovedEventImpl event = null;
        if (internalEvent.getEntity() instanceof IdentityLink) {
            IdentityLink identityLink = (IdentityLink) internalEvent.getEntity();
            if (processCandidateStarterEventConverterHelper.isProcessUserCandidateStarterLink(identityLink)) {
                event = new ProcessUserCandidateStarterRemovedEventImpl(converter.from(identityLink));
            }
        }
        return Optional.ofNullable(event);
    }

}
