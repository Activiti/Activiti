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
package org.activiti.spring.boot.process.listener;

import org.activiti.api.process.model.ProcessCandidateStarterUser;
import org.activiti.api.process.runtime.events.ProcessCandidateStarterUserRemovedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessCandidateStarterUserRemovedListener implements ProcessRuntimeEventListener<ProcessCandidateStarterUserRemovedEvent> {

    private List<ProcessCandidateStarterUser> candidateStarterUsers = new ArrayList<>();

    @Override
    public void onEvent(ProcessCandidateStarterUserRemovedEvent event) {
        candidateStarterUsers.add(event.getEntity());
    }

    public List<ProcessCandidateStarterUser> getCandidateStarterUsers() {
        return candidateStarterUsers;
    }

}
