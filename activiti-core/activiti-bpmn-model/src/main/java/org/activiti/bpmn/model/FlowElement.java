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
package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class FlowElement extends BaseElement implements HasExecutionListeners, AcceptUpdates {

  protected String name;
  protected String documentation;
  protected List<ActivitiListener> executionListeners = new ArrayList<ActivitiListener>();
  protected FlowElementsContainer parentContainer;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDocumentation() {
    return documentation;
  }

  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }

  public List<ActivitiListener> getExecutionListeners() {
    return executionListeners;
  }

  public void setExecutionListeners(List<ActivitiListener> executionListeners) {
    this.executionListeners = executionListeners;
  }

  @JsonIgnore
  public FlowElementsContainer getParentContainer() {
    return parentContainer;
  }

  @JsonIgnore
  public SubProcess getSubProcess() {
    SubProcess subProcess = null;
    if (parentContainer instanceof SubProcess) {
      subProcess = (SubProcess) parentContainer;
    }

    return subProcess;
  }

  public void setParentContainer(FlowElementsContainer parentContainer) {
    this.parentContainer = parentContainer;
  }

  public abstract FlowElement clone();

  public void setValues(FlowElement otherElement) {
    super.setValues(otherElement);
    setName(otherElement.getName());
    setDocumentation(otherElement.getDocumentation());

    executionListeners = new ArrayList<ActivitiListener>();
    if (otherElement.getExecutionListeners() != null && !otherElement.getExecutionListeners().isEmpty()) {
      for (ActivitiListener listener : otherElement.getExecutionListeners()) {
        executionListeners.add(listener.clone());
      }
    }
  }
}
