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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.activiti.spring.autodeployment.DefaultAutoDeploymentStrategy;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultAutoDeploymentStrategyTest extends AbstractAutoDeploymentStrategyTest {

    private DefaultAutoDeploymentStrategy deploymentStrategy;

    @Before
    public void before() throws Exception {
        super.before();
        deploymentStrategy = new DefaultAutoDeploymentStrategy(applicationUpgradeContextServiceMock);
    }

    @Test
    public void testHandlesMode() {
        assertThat(deploymentStrategy.handlesMode(DefaultAutoDeploymentStrategy.DEPLOYMENT_MODE)).isTrue();
        assertThat(deploymentStrategy.handlesMode("other-mode")).isFalse();
        assertThat(deploymentStrategy.handlesMode(null)).isFalse();
    }

    @Test
    public void testDeployResources() {
        final Resource[] resources = new Resource[]{resourceMock1, resourceMock2, resourceMock3, resourceMock4, resourceMock5};
        deploymentStrategy.deployResources(deploymentNameHint,
                                           resources,
                                           repositoryServiceMock);

        verify(repositoryServiceMock).createDeployment();
        verify(deploymentBuilderMock).enableDuplicateFiltering();
        verify(deploymentBuilderMock).name(deploymentNameHint);
        verify(deploymentBuilderMock).addInputStream(eq(resourceName1),
                                        isA(Resource.class));
        verify(deploymentBuilderMock).addInputStream(eq(resourceName2),
                                        isA(Resource.class));
        verify(deploymentBuilderMock).addInputStream(eq(resourceName3),
                                        isA(Resource.class));
        verify(deploymentBuilderMock).addInputStream(eq(resourceName4),
                                        isA(Resource.class));
        verify(deploymentBuilderMock).addInputStream(eq(resourceName5),
                                        isA(Resource.class));
        verify(deploymentBuilderMock).deploy();
    }

    @Test
    public void testDeployResourcesNoResources() {
        final Resource[] resources = new Resource[]{};
        deploymentStrategy.deployResources(deploymentNameHint,
                                           resources,
                                           repositoryServiceMock);

        verify(repositoryServiceMock,
               times(1)).createDeployment();
        verify(deploymentBuilderMock,
               times(1)).enableDuplicateFiltering();
        verify(deploymentBuilderMock,
               times(1)).name(deploymentNameHint);
        verify(deploymentBuilderMock,
               never()).addInputStream(isA(String.class),
                                       isA(InputStream.class));
        verify(deploymentBuilderMock,
               never()).addInputStream(eq(resourceName2),
                                       isA(InputStream.class));
        verify(deploymentBuilderMock,
               never()).addZipInputStream(isA(ZipInputStream.class));
        verify(deploymentBuilderMock,
               times(1)).deploy();
    }

    @Test
    public void testDetermineResourceNameWithExceptionFailsGracefully() throws Exception {
        when(resourceMock3.getFile()).thenThrow(new IOException());
        when(resourceMock3.getFilename()).thenReturn(resourceName3);

        final Resource[] resources = new Resource[]{resourceMock3};
        deploymentStrategy.deployResources(deploymentNameHint,
                                           resources,
                                           repositoryServiceMock);
    }

}
