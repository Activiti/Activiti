/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.events.ProcessDeployedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.springframework.stereotype.Component;

@Component
public class DeployedProcessesListener implements ProcessRuntimeEventListener<ProcessDeployedEvent> {

    private List<ProcessDefinition> deployedProcesses = new ArrayList<>();
    private Map<String, String> processModelContents = new HashMap<>();

    @Override
    public void onEvent(ProcessDeployedEvent event) {
        deployedProcesses.add(event.getEntity());
        processModelContents.put(event.getProcessDefinitionKey(), event.getProcessModelContent());
    }

    public List<ProcessDefinition> getDeployedProcesses() {
        return deployedProcesses;
    }

    public Map<String, String> getProcessModelContents() {
        return processModelContents;
    }
}
