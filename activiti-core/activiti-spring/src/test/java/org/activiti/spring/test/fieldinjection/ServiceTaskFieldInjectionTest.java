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

package org.activiti.spring.test.fieldinjection;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;


/**
 */
@ContextConfiguration("classpath:org/activiti/spring/test/fieldinjection/fieldInjectionSpringTest-context.xml")
public class ServiceTaskFieldInjectionTest extends SpringActivitiTestCase {

    @Deployment
    public void testDelegateExpressionWithSingletonBean() {
      runtimeService.startProcessInstanceByKey("delegateExpressionSingleton", singletonMap("input", 100));
      Task task = taskService.createTaskQuery().singleResult();
      Map<String, Object> variables = taskService.getVariables(task.getId());

      Integer resultServiceTask1 = (Integer) variables.get("resultServiceTask1");
      assertThat(resultServiceTask1.intValue()).isEqualTo(202);

      Integer resultServiceTask2 = (Integer) variables.get("resultServiceTask2");
      assertThat(resultServiceTask2.intValue()).isEqualTo(579);

      // Verify only one instance was created
      assertThat(SingletonDelegateExpressionBean.INSTANCE_COUNT.get()).isEqualTo(1);
    }

    @Deployment
    public void testDelegateExpressionWithPrototypeBean() {
      runtimeService.startProcessInstanceByKey("delegateExpressionPrototype", singletonMap("input", 100));
      Task task = taskService.createTaskQuery().singleResult();
      Map<String, Object> variables = taskService.getVariables(task.getId());

      Integer resultServiceTask1 = (Integer) variables.get("resultServiceTask1");
      assertThat(resultServiceTask1.intValue()).isEqualTo(202);

      Integer resultServiceTask2 = (Integer) variables.get("resultServiceTask2");
      assertThat(resultServiceTask2.intValue()).isEqualTo(579);

      // Verify TWO INSTANCES were created
      assertThat(PrototypeDelegateExpressionBean.INSTANCE_COUNT.get()).isEqualTo(2);
    }

}
