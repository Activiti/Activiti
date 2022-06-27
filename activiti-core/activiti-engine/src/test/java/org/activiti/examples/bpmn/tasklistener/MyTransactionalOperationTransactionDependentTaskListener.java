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

package org.activiti.examples.bpmn.tasklistener;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.Task;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoryLevel;


public class MyTransactionalOperationTransactionDependentTaskListener extends CurrentTaskTransactionDependentTaskListener {

  @Override
  public void notify(String processInstanceId, String executionId, Task task, Map<String, Object> executionVariables, Map<String, Object> customPropertiesMap) {
    super.notify(processInstanceId, executionId, task, executionVariables, customPropertiesMap);

    if (Context.getCommandContext().getProcessEngineConfiguration().getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoryService historyService = Context.getCommandContext().getProcessEngineConfiguration().getHistoryService();

      // delete first historic instance
      List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
      historyService.deleteHistoricProcessInstance(historicProcessInstances.get(0).getId());
    }
  }
}
