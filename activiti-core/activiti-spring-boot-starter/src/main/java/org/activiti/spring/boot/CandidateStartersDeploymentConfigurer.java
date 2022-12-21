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

import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeploymentHelper;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.spring.SpringProcessEngineConfiguration;

import java.util.List;

public class CandidateStartersDeploymentConfigurer implements ProcessEngineConfigurationConfigurer {

    private static final String EVERYONE_GROUP = "*";

    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
        processEngineConfiguration.setBpmnDeploymentHelper(new CandidateStartersDeploymentHelper());
    }

    public class CandidateStartersDeploymentHelper extends BpmnDeploymentHelper {
        @Override
        public void addAuthorizationsForNewProcessDefinition(Process process, ProcessDefinitionEntity processDefinition) {
            super.addAuthorizationsForNewProcessDefinition(process, processDefinition);
            if (process != null &&
                !process.isCandidateStarterUsersDefined() &&
                !process.isCandidateStarterGroupsDefined()) {
                addAuthorizationsFromIterator(Context.getCommandContext(), List.of(EVERYONE_GROUP), processDefinition, ExpressionType.GROUP);
            }
        }
    }
}
