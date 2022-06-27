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

import java.util.List;

import org.activiti.spring.autodeployment.NeverFailAutoDeploymentStrategy;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:org/activiti/spring/test/autodeployment/errorHandling/spring-context.xml")
public class NeverFailAutoDeploymentStrategyTest extends SpringActivitiTestCase {

    private final String nameHint = "NeverFailAutoDeploymentStrategyTest";

    private final String validName1 = "org/activiti/spring/test/autodeployment/errorHandling/valid.bpmn20.xml";
    private final String invalidName1 = "org/activiti/spring/test/autodeployment/errorHandling/parsing-error.bpmn20.xml";
    private final String invalidName2 = "org/activiti/spring/test/autodeployment/errorHandling/validation-error.bpmn20.xml";

    private void cleanUp() {
        List<org.activiti.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.activiti.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    @Override
    public void setUp(){
        cleanUp();
    }

    @Override
    public void tearDown(){
        cleanUp();
    }

    @Test
    public void testValidResources() {
        final Resource[] resources = new Resource[]{new ClassPathResource(validName1)};
        NeverFailAutoDeploymentStrategy deploymentStrategy = new NeverFailAutoDeploymentStrategy(null);
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
    }

    @Test
    public void testInvalidResources() {
        final Resource[] resources = new Resource[]{new ClassPathResource(validName1), new ClassPathResource(invalidName1), new ClassPathResource(invalidName2)};
        NeverFailAutoDeploymentStrategy deploymentStrategy = new NeverFailAutoDeploymentStrategy(null);
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
    }

    @Test
    public void testWithParsingErrorResources() {
        final Resource[] resources = new Resource[]{new ClassPathResource(validName1), new ClassPathResource(invalidName1)};
        NeverFailAutoDeploymentStrategy deploymentStrategy = new NeverFailAutoDeploymentStrategy(null);
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
    }

    @Test
    public void testWithValidationErrorResources() {
        final Resource[] resources = new Resource[]{new ClassPathResource(validName1), new ClassPathResource(invalidName2)};
        NeverFailAutoDeploymentStrategy deploymentStrategy = new NeverFailAutoDeploymentStrategy(null);
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
    }

    @Test
    public void testOnlyInvalidResources() {
        final Resource[] resources = new Resource[]{new ClassPathResource(invalidName1)};
        NeverFailAutoDeploymentStrategy deploymentStrategy = new NeverFailAutoDeploymentStrategy(null);
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(0);
    }
}
