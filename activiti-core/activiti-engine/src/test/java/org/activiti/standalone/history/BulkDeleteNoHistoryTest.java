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

package org.activiti.standalone.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class BulkDeleteNoHistoryTest extends ResourceActivitiTestCase {

  public BulkDeleteNoHistoryTest() {
    // History needs to be disabled to prevent any historic entities come in
    // between the variable deletes
    // to make sure a single batch is used for all entities
    super("org/activiti/standalone/history/nohistory.activiti.cfg.xml");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testLargeAmountOfVariableBulkDelete() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();

    // Do a bulk-update with a number higher than any DB's magic numbers
    for (int i = 0; i < 4001; i++) {
      variables.put("var" + i, i);
    }

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    // Completing the task will cause a bulk delete of 4001 entities
    taskService.complete(task.getId());

    // Check if process is gone
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0L);
  }
}
