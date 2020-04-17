/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.application.deployer;

import org.activiti.application.ApplicationContent;
import org.activiti.application.ApplicationEntry;
import org.activiti.application.FileContent;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProcessEntryDeployerTest {

    @InjectMocks
    private ProcessEntryDeployer deployer;

    @Mock
    private RepositoryService repositoryService;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void deployEntriesShouldDelegateDeploymentToDeployBuilder() {
        //given
        ApplicationContent applicationContent = new ApplicationContent();
        FileContent fileContent = new FileContent("process",
                                              "any".getBytes());
        applicationContent.add(new ApplicationEntry("processes",
                                                    fileContent));

        DeploymentBuilder deploymentBuilder = mock(DeploymentBuilder.class,
                                      Answers.RETURNS_SELF);
        given(repositoryService.createDeployment()).willReturn(deploymentBuilder);

        //when
        deployer.deployEntries(applicationContent);

        //then
        InOrder inOrder = inOrder(deploymentBuilder);
        inOrder.verify(deploymentBuilder).addBytes(fileContent.getName(), fileContent.getContent());
        inOrder.verify(deploymentBuilder).deploy();
    }
}
