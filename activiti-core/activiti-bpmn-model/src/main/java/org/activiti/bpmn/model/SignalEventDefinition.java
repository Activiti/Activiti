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

public class SignalEventDefinition extends EventDefinition {

  protected String signalRef;
  protected String signalExpression;
  protected boolean async;

  public String getSignalRef() {
    return signalRef;
  }

  public void setSignalRef(String signalRef) {
    this.signalRef = signalRef;
  }

  public String getSignalExpression() {
    return signalExpression;
  }

  public void setSignalExpression(String signalExpression) {
    this.signalExpression = signalExpression;
  }

  public boolean isAsync() {
    return async;
  }

  public void setAsync(boolean async) {
    this.async = async;
  }

  public SignalEventDefinition clone() {
    SignalEventDefinition clone = new SignalEventDefinition();
    clone.setValues(this);
    return clone;
  }

  public void setValues(SignalEventDefinition otherDefinition) {
    super.setValues(otherDefinition);
    setSignalRef(otherDefinition.getSignalRef());
    setSignalExpression(otherDefinition.getSignalExpression());
    setAsync(otherDefinition.isAsync());
  }
}
