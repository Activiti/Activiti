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
package org.activiti.bpmn.model.alfresco;

import org.activiti.bpmn.model.StartEvent;

public class AlfrescoStartEvent extends StartEvent {

  protected String runAs;
  protected String scriptProcessor;

  public String getRunAs() {
    return runAs;
  }

  public void setRunAs(String runAs) {
    this.runAs = runAs;
  }

  public String getScriptProcessor() {
    return scriptProcessor;
  }

  public void setScriptProcessor(String scriptProcessor) {
    this.scriptProcessor = scriptProcessor;
  }

  public AlfrescoStartEvent clone() {
    AlfrescoStartEvent clone = new AlfrescoStartEvent();
    clone.setValues(this);
    return clone;
  }

  public void setValues(AlfrescoStartEvent otherElement) {
    super.setValues(otherElement);
    setRunAs(otherElement.getRunAs());
    setScriptProcessor(otherElement.getScriptProcessor());
  }
}
