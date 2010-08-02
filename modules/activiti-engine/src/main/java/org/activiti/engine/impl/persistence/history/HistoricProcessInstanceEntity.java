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

package org.activiti.engine.impl.persistence.history;

import java.util.Date;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;

/**
 * @author Christian Stettler
 */
public class HistoricProcessInstanceEntity extends HistoricScopeInstanceEntity implements HistoricProcessInstance {

  private String endStateName;

  protected HistoricProcessInstanceEntity() {
    // for ibatis
  }

  public HistoricProcessInstanceEntity(String processInstanceId, String processDefinitionId, Date startTime) {
    super(processInstanceId, processDefinitionId, startTime);
  }

  public void markEnded(Date endTime, String endStateName) {
    internalMarkEnded(endTime);

    if (endStateName == null) {
      throw new IllegalArgumentException("End state name must not be null");
    }

    this.endStateName = endStateName;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Object getPersistentState() {
    Map<String, Object> persistentState = (Map<String, Object>) super.getPersistentState();
    persistentState.put("endStateName", endStateName);
    return persistentState;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getEndStateName() {
    return endStateName;
  }
}
