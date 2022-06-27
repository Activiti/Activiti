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

package org.activiti.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

public class ProcessInstanceUpdateBusinessKeyTest extends PluggableActivitiTestCase {

  @Deployment
  public void testProcessInstanceUpdateBusinessKey() {
    runtimeService.startProcessInstanceByKey("businessKeyProcess");

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance.getBusinessKey()).isEqualTo("bzKey");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
      assertThat(historicProcessInstance.getBusinessKey()).isEqualTo("bzKey");
    }
  }

  @Deployment
  public void testUpdateExistingBusinessKey() {
    runtimeService.startProcessInstanceByKey("businessKeyProcess", "testKey");

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance.getBusinessKey()).isEqualTo("testKey");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
      assertThat(historicProcessInstance.getBusinessKey()).isEqualTo("testKey");
    }

    runtimeService.updateBusinessKey(processInstance.getId(), "newKey");

    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance.getBusinessKey()).isEqualTo("newKey");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
      assertThat(historicProcessInstance.getBusinessKey()).isEqualTo("newKey");
    }
  }

  public static class UpdateBusinessKeyExecutionListener implements ExecutionListener {

    private static final long serialVersionUID = 1L;

    public void notify(DelegateExecution delegateExecution) {
      Context.getCommandContext().getExecutionEntityManager().updateProcessInstanceBusinessKey((ExecutionEntity)delegateExecution, "bzKey");
    }
  }

}
