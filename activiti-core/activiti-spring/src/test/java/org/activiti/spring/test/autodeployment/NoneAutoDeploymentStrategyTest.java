/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.activiti.engine.RepositoryService;
import org.activiti.spring.autodeployment.NoneAutoDeploymentStrategy;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

public class NoneAutoDeploymentStrategyTest {

    private NoneAutoDeploymentStrategy deploymentStrategy;

    @Before
    public void before() {
        deploymentStrategy = new NoneAutoDeploymentStrategy(mock());
    }

    @Test
    public void testHandlesMode() {
        assertThat(deploymentStrategy.handlesMode(NoneAutoDeploymentStrategy.DEPLOYMENT_MODE)).isTrue();
        assertThat(deploymentStrategy.handlesMode("other-mode")).isFalse();
        assertThat(deploymentStrategy.handlesMode(null)).isFalse();
    }

    @Test
    public void testDeployResourcesDoesNotInteractWithRepositoryService() {
        final Resource[] resources = new Resource[] {};
        RepositoryService repositoryService = mock();

        deploymentStrategy.deployResources("deploymentNameHint", resources, repositoryService);

        verifyNoInteractions(repositoryService);
    }
}
