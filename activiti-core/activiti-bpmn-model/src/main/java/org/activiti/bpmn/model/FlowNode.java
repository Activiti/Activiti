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
package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.CollectionUtils;

public abstract class FlowNode extends FlowElement {

  protected boolean asynchronous;
  protected boolean notExclusive;

  protected List<SequenceFlow> incomingFlows = new ArrayList<SequenceFlow>();
  protected List<SequenceFlow> outgoingFlows = new ArrayList<SequenceFlow>();

  @JsonIgnore
  protected Object behavior;

  public FlowNode() {

    }

 public boolean isAsynchronous() {
    return asynchronous;
  }

  public void setAsynchronous(boolean asynchronous) {
    this.asynchronous = asynchronous;
  }

  public boolean isExclusive() {
    return !notExclusive;
  }

  public void setExclusive(boolean exclusive) {
    this.notExclusive = !exclusive;
  }

  public boolean isNotExclusive() {
    return notExclusive;
  }
  public void setNotExclusive(boolean notExclusive) {
    this.notExclusive = notExclusive;
  }

  public Object getBehavior() {
    return behavior;
  }

  public void setBehavior(Object behavior) {
    this.behavior = behavior;
  }

  public List<SequenceFlow> getIncomingFlows() {
    return incomingFlows;
  }

  public void setIncomingFlows(List<SequenceFlow> incomingFlows) {
    this.incomingFlows = incomingFlows;
  }

  public List<SequenceFlow> getOutgoingFlows() {
    return outgoingFlows;
  }

  public void setOutgoingFlows(List<SequenceFlow> outgoingFlows) {
    this.outgoingFlows = outgoingFlows;
  }

  public void setValues(FlowNode otherNode) {
    super.setValues(otherNode);
    setAsynchronous(otherNode.isAsynchronous());
    setNotExclusive(otherNode.isNotExclusive());
  }

  public boolean hasIncomingFlows() {
    return (CollectionUtils.isNotEmpty(this.incomingFlows));
  }

  public boolean hasOutgoingFlows() {
    return (CollectionUtils.isNotEmpty(this.outgoingFlows));
  }

  public boolean isLinkCatchEvent() {
    if (this instanceof IntermediateCatchEvent intermediateCatchEvent) {
        return intermediateCatchEvent.isLinkEvent();
    }
    return false;
  }

  public boolean isLinkThrowEvent() {
    if (this instanceof ThrowEvent throwEvent) {
        return throwEvent.isLinkEvent();
    }
    return false;
  }

  public boolean isInitialFlowNode() {
    return (CollectionUtils.isEmpty(this.getIncomingFlows())
        && this.getSubProcess() == null && !this.isLinkCatchEvent());
  }
}
