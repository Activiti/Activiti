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

package org.activiti.standalone.idgenerator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

/**

 */
public class UuidGeneratorTest extends ResourceActivitiTestCase {

  public UuidGeneratorTest() {
    super("org/activiti/standalone/idgenerator/uuidgenerator.test.activiti.cfg.xml");
  }

  @Deployment
  public void testUuidGeneratorUsage() throws Exception {

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    // Start processes
    for (int i = 0; i < 50; i++) {
      executorService.execute(() -> runtimeService.startProcessInstanceByKey("simpleProcess"));
    }

    // Complete tasks
    executorService.execute(() -> {
      boolean tasksFound = true;
      while (tasksFound) {
        List<Task> tasks = taskService.createTaskQuery().list();
        for (Task task : tasks) {
          taskService.complete(task.getId());
        }

        tasksFound = taskService.createTaskQuery().count() > 0;

        if (!tasksFound) {
            try {
                Awaitility.await().atMost(500L, TimeUnit.MILLISECONDS).until(() -> taskService.createTaskQuery().count() > 0);
                tasksFound = true;
            } catch (ConditionTimeoutException cte) {
                // expected exception
            }
        }
      }
    });

    executorService.shutdown();
    executorService.awaitTermination(1, TimeUnit.MINUTES);

    assertThat(historyService.createHistoricProcessInstanceQuery().count()).isEqualTo(50);
  }

}
