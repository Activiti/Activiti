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



public class ProcessDefinitionInfoEntityImpl extends AbstractEntity implements ProcessDefinitionInfoEntity, Serializable {

  private static final long serialVersionUID = 1L;

  protected String processDefinitionId;
  protected String infoJsonId;

  public ProcessDefinitionInfoEntityImpl() {

  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("processDefinitionId", this.processDefinitionId);
    persistentState.put("infoJsonId", this.infoJsonId);
    return persistentState;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getInfoJsonId() {
    return infoJsonId;
  }

  public void setInfoJsonId(String infoJsonId) {
    this.infoJsonId = infoJsonId;
  }
}
