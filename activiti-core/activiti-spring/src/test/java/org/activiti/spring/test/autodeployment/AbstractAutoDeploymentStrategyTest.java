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


package org.activiti.spring.test.autodeployment;

import java.io.File;
import java.io.InputStream;

import org.activiti.core.common.spring.project.ApplicationUpgradeContextService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractAutoDeploymentStrategyTest {

    @Mock
    protected RepositoryService repositoryServiceMock;

    @Mock
    protected DeploymentBuilder deploymentBuilderMock;

    @Mock
    protected ContextResource resourceMock1;

    @Mock
    protected ByteArrayResource resourceMock2;

    @Mock
    protected Resource resourceMock3;

    @Mock
    protected Resource resourceMock4;

    @Mock
    protected Resource resourceMock5;

    @Mock
    protected File fileMock1;

    @Mock
    protected File fileMock2;

    @Mock
    protected File fileMock3;

    @Mock
    protected File fileMock4;

    @Mock
    protected File fileMock5;

    @Mock
    private Deployment deploymentMock;

    @Mock
    protected ApplicationUpgradeContextService applicationUpgradeContextServiceMock;

    protected final String deploymentNameHint = "nameHint";

    protected final String resourceName1 = "resourceName1.bpmn";
    protected final String resourceName2 = "resourceName2.bpmn";
    protected final String resourceName3 = "/opt/processes/resourceName3.bar";
    protected final String resourceName4 = "/opt/processes/resourceName4.zip";
    protected final String resourceName5 = "/opt/processes/resourceName5.jar";

    @Before
    public void before() throws Exception {
        when(resourceMock1.getPathWithinContext()).thenReturn(resourceName1);
        when(resourceMock2.getDescription()).thenReturn(resourceName2);

        when(resourceMock3.getFile()).thenReturn(fileMock3);
        when(fileMock3.getAbsolutePath()).thenReturn(resourceName3);

        when(resourceMock4.getFile()).thenReturn(fileMock4);
        when(fileMock4.getAbsolutePath()).thenReturn(resourceName4);

        when(resourceMock5.getFile()).thenReturn(fileMock5);
        when(fileMock5.getAbsolutePath()).thenReturn(resourceName5);

        when(repositoryServiceMock.createDeployment()).thenReturn(deploymentBuilderMock);
        when(deploymentBuilderMock.enableDuplicateFiltering()).thenReturn(deploymentBuilderMock);
        when(deploymentBuilderMock.name(isA(String.class))).thenReturn(deploymentBuilderMock);

        when(deploymentBuilderMock.deploy()).thenReturn(deploymentMock);
    }

}
