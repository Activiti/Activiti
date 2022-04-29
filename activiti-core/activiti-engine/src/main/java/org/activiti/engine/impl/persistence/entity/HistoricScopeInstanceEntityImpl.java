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


package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.Date;

import org.activiti.engine.impl.context.Context;


public abstract class HistoricScopeInstanceEntityImpl extends AbstractEntityNoRevision implements HistoricScopeInstanceEntity, Serializable {

  private static final long serialVersionUID = 1L;

  protected String processInstanceId;
  protected String processDefinitionId;
  protected Date startTime;
  protected Date endTime;
  protected Long durationInMillis;
  protected String deleteReason;

  public void markEnded(String deleteReason) {
    if (this.endTime == null) {
      this.deleteReason = deleteReason;
      this.endTime = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
      this.durationInMillis = endTime.getTime() - startTime.getTime();
    }
  }

  // getters and setters ////////////////////////////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public Long getDurationInMillis() {
    return durationInMillis;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public void setDurationInMillis(Long durationInMillis) {
    this.durationInMillis = durationInMillis;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }
}
