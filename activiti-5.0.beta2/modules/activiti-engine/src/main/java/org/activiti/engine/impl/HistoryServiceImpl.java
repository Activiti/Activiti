/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;

/**
 * @author Tom Baeyens
 * @author Christian Stettler
 */
public class HistoryServiceImpl extends ServiceImpl implements HistoryService {

  public HistoricProcessInstance findHistoricProcessInstanceById(final String processInstanceId) {
    if(processInstanceId == null) {
        throw new ActivitiException("processInstanceId is null");
    }
    return createHistoricProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();
  }

  public HistoricProcessInstanceQuery createHistoricProcessInstanceQuery() {
    return new HistoricProcessInstanceQueryImpl(commandExecutor);
  }

  public HistoricActivityInstanceQuery createHistoricActivityInstanceQuery() {
    return new HistoricActivityInstanceQueryImpl(commandExecutor);
  }
}
