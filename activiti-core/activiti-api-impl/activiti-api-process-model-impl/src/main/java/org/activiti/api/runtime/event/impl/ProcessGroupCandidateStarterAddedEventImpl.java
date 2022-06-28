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
package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.ProcessGroupCandidateStarter;
import org.activiti.api.process.model.events.ProcessGroupCandidateStarterEvent;
import org.activiti.api.process.runtime.events.ProcessGroupCandidateStarterAddedEvent;

public class ProcessGroupCandidateStarterAddedEventImpl extends RuntimeEventImpl<ProcessGroupCandidateStarter, ProcessGroupCandidateStarterEvent.ProcessGroupCandidateStarterEvents> implements ProcessGroupCandidateStarterAddedEvent {

    public ProcessGroupCandidateStarterAddedEventImpl() {
    }

    public ProcessGroupCandidateStarterAddedEventImpl(ProcessGroupCandidateStarter entity) {
        super(entity);
    }

    @Override
    public ProcessGroupCandidateStarterEvents getEventType() {
        return ProcessGroupCandidateStarterEvents.PROCESS_GROUP_CANDIDATE_STARER_ADDED;
    }
}
