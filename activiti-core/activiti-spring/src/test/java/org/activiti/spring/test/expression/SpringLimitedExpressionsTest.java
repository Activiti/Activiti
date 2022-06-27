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


package org.activiti.spring.test.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test limiting the exposed beans in expressions.
 */
@ContextConfiguration("classpath:org/activiti/spring/test/expression/expressionLimitedBeans-context.xml")
public class SpringLimitedExpressionsTest extends SpringActivitiTestCase {

    private void cleanUp() {
        List<org.activiti.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.activiti.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    @Override
    public void tearDown() {
        cleanUp();
    }

    @Deployment
    public void testLimitedBeansExposed() throws Exception {
        // Start process, which has a service-task which calls 'bean1', which is exposed
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("limitedExpressionProcess");

        String beanOutput = (String) runtimeService.getVariable(processInstance.getId(),
                                                                "beanOutput");
        assertThat(beanOutput).isNotNull();
        assertThat(beanOutput).isEqualTo("Activiti BPMN 2.0 process engine");

        // Finish the task, should continue to serviceTask which uses a bean that is present
        // in application-context, but not exposed explicitly in "beans", should throw error!
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task).isNotNull();

        assertThatExceptionOfType(ActivitiException.class)
          .isThrownBy(() -> taskService.complete(task.getId()))
          .satisfies(ae -> assertThat(ae.getCause()).hasMessageContaining("Unknown property used in expression"));
    }
}
