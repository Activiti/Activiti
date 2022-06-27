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

public class Operation extends BaseElement {

  protected String name;
  protected String implementationRef;
  protected String inMessageRef;
  protected String outMessageRef;
  protected List<String> errorMessageRef = new ArrayList<String>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImplementationRef() {
    return implementationRef;
  }

  public void setImplementationRef(String implementationRef) {
    this.implementationRef = implementationRef;
  }

  public String getInMessageRef() {
    return inMessageRef;
  }

  public void setInMessageRef(String inMessageRef) {
    this.inMessageRef = inMessageRef;
  }

  public String getOutMessageRef() {
    return outMessageRef;
  }

  public void setOutMessageRef(String outMessageRef) {
    this.outMessageRef = outMessageRef;
  }

  public List<String> getErrorMessageRef() {
    return errorMessageRef;
  }

  public void setErrorMessageRef(List<String> errorMessageRef) {
    this.errorMessageRef = errorMessageRef;
  }

  public Operation clone() {
    Operation clone = new Operation();
    clone.setValues(this);
    return clone;
  }

  public void setValues(Operation otherElement) {
    super.setValues(otherElement);
    setName(otherElement.getName());
    setImplementationRef(otherElement.getImplementationRef());
    setInMessageRef(otherElement.getInMessageRef());
    setOutMessageRef(otherElement.getOutMessageRef());

    errorMessageRef = new ArrayList<String>();
    if (otherElement.getErrorMessageRef() != null && !otherElement.getErrorMessageRef().isEmpty()) {
      errorMessageRef.addAll(otherElement.getErrorMessageRef());
    }
  }
}
