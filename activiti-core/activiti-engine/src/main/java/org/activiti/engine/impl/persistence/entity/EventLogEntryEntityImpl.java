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

import java.util.Date;

/**
 * An event log entry can only be inserted (and maybe deleted).
 *

 */
public class EventLogEntryEntityImpl extends AbstractEntityNoRevision implements EventLogEntryEntity {

  protected long logNumber; // cant use id here, it would clash with entity
  protected String type;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String executionId;
  protected String taskId;
  protected Date timeStamp;
  protected String userId;
  protected byte[] data;
  protected String lockOwner;
  protected String lockTime;
  protected int isProcessed;

  public EventLogEntryEntityImpl() {
  }

  @Override
  public Object getPersistentState() {
    return null; // Not updateable
  }

  public long getLogNumber() {
    return logNumber;
  }

  public void setLogNumber(long logNumber) {
    this.logNumber = logNumber;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public Date getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(Date timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }

  public String getLockTime() {
    return lockTime;
  }

  public void setLockTime(String lockTime) {
    this.lockTime = lockTime;
  }

  public int getProcessed() {
    return isProcessed;
  }

  public void setProcessed(int isProcessed) {
    this.isProcessed = isProcessed;
  }

  @Override
  public String toString() {
    return timeStamp.toString() + " : " + type;
  }

}
