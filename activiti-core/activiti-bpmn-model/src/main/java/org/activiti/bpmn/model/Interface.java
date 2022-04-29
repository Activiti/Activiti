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

public class Interface extends BaseElement {

  protected String name;
  protected String implementationRef;
  protected List<Operation> operations = new ArrayList<Operation>();

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

  public List<Operation> getOperations() {
    return operations;
  }

  public void setOperations(List<Operation> operations) {
    this.operations = operations;
  }

  public Interface clone() {
    Interface clone = new Interface();
    clone.setValues(this);
    return clone;
  }

  public void setValues(Interface otherElement) {
    super.setValues(otherElement);
    setName(otherElement.getName());
    setImplementationRef(otherElement.getImplementationRef());

    operations = new ArrayList<Operation>();
    if (otherElement.getOperations() != null && !otherElement.getOperations().isEmpty()) {
      for (Operation operation : otherElement.getOperations()) {
        operations.add(operation.clone());
      }
    }
  }
}
