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
package org.activiti.application.deployer;

import java.util.List;

import org.activiti.application.ApplicationContent;
import org.activiti.application.FileContent;
import org.activiti.application.discovery.ProcessEntryDiscovery;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;

public class ProcessEntryDeployer implements ApplicationEntryDeployer {

    private RepositoryService repositoryService;

    public ProcessEntryDeployer(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public void deployEntries(ApplicationContent application) {
        List<FileContent> processContents = application.getFileContents(ProcessEntryDiscovery.PROCESSES);
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().enableDuplicateFiltering().name("ApplicationAutoDeployment");
        for (FileContent processContent : processContents) {
            deploymentBuilder.addBytes(processContent.getName(), processContent.getContent());
        }
        deploymentBuilder.deploy();
    }
}
