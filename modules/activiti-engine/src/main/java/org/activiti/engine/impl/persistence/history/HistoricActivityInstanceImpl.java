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

import org.activiti.engine.history.HistoricActivityInstance;

/**
 * @author Christian Stettler
 */
public class HistoricActivityInstanceImpl extends HistoricScopeInstanceImpl implements HistoricActivityInstance {

  private String activityId;
  private String activityName;
  private String activityType;

  protected HistoricActivityInstanceImpl() {
    // for ibatis
  }

  public HistoricActivityInstanceImpl(String activityId, String activityName, String activityType, String processInstanceId, String processDefinitionId, Date startTime) {
    super(processInstanceId, processDefinitionId, startTime);

    if (activityId == null) {
      throw new IllegalArgumentException("Activity id must not be null");
    }
    if (activityType == null) {
      throw new IllegalArgumentException("Activity type must not be null");
    }

    this.activityId = activityId;
    this.activityName = activityName;
    this.activityType = activityType;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getActivityId() {
    return activityId;
  }
  public String getActivityName() {
    return activityName;
  }

  public String getActivityType() {
    return activityType;
  }

  public void markEnded(Date endTime) {
    internalMarkEnded(endTime);
  }
}
