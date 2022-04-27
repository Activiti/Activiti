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

package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.db.BulkDeleteable;


public class HistoricIdentityLinkEntityImpl extends AbstractEntityNoRevision implements HistoricIdentityLinkEntity, Serializable, BulkDeleteable {

  private static final long serialVersionUID = 1L;

  protected String type;
  protected String userId;
  protected String groupId;
  protected String taskId;
  protected String processInstanceId;

  public HistoricIdentityLinkEntityImpl() {

  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("id", this.id);
    persistentState.put("type", this.type);

    if (this.userId != null) {
      persistentState.put("userId", this.userId);
    }

    if (this.groupId != null) {
      persistentState.put("groupId", this.groupId);
    }

    if (this.taskId != null) {
      persistentState.put("taskId", this.taskId);
    }

    if (this.processInstanceId != null) {
      persistentState.put("processInstanceId", this.processInstanceId);
    }

    return persistentState;
  }

  public boolean isUser() {
    return userId != null;
  }

  public boolean isGroup() {
    return groupId != null;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    if (this.groupId != null && userId != null) {
      throw new ActivitiException("Cannot assign a userId to a task assignment that already has a groupId");
    }
    this.userId = userId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    if (this.userId != null && groupId != null) {
      throw new ActivitiException("Cannot assign a groupId to a task assignment that already has a userId");
    }
    this.groupId = groupId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
}
