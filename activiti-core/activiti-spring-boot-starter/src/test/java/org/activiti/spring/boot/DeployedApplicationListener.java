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
package org.activiti.spring.boot;

import java.util.ArrayList;
import java.util.List;
import org.activiti.api.process.model.Deployment;
import org.activiti.api.process.model.events.ApplicationDeployedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.springframework.stereotype.Component;

@Component
public class DeployedApplicationListener implements ProcessRuntimeEventListener<ApplicationDeployedEvent> { 
    
    private List<Deployment> deployedApplications = new ArrayList<>();
    
    @Override
    public void onEvent(ApplicationDeployedEvent event) {
            deployedApplications.add(event.getEntity()); 
    }
    
    public List<Deployment> getDeployedApplication() {
            return deployedApplications; 
    }
}
