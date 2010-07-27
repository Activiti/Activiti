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

package org.activiti.impl.history;

import java.util.Date;

import org.activiti.history.HistoricProcessInstance;

/**
 * @author Christian Stettler
 */
public class HistoricProcessInstanceImpl extends AbstractHistoricInstanceImpl implements HistoricProcessInstance {

  private String id;
  private String endStateName;

  protected HistoricProcessInstanceImpl() {
    // for ibatis
  }

  public HistoricProcessInstanceImpl(String processInstanceId, String processDefinitionId, Date startTime) {
    super(processInstanceId, processDefinitionId, startTime);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEndStateName() {
    return endStateName;
  }

  public void markEnded(Date endTime, String endStateName) {
    internalMarkEnded(endTime);

    if (endStateName == null) {
      throw new IllegalArgumentException("End state name must not be null");
    }

    this.endStateName = endStateName;
  }

}
