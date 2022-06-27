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

public class Association extends Artifact {

  protected AssociationDirection associationDirection = AssociationDirection.NONE;
  protected String sourceRef;
  protected String targetRef;

  public AssociationDirection getAssociationDirection() {
    return associationDirection;
  }

  public void setAssociationDirection(AssociationDirection associationDirection) {
    this.associationDirection = associationDirection;
  }

  public String getSourceRef() {
    return sourceRef;
  }

  public void setSourceRef(String sourceRef) {
    this.sourceRef = sourceRef;
  }

  public String getTargetRef() {
    return targetRef;
  }

  public void setTargetRef(String targetRef) {
    this.targetRef = targetRef;
  }

  public Association clone() {
    Association clone = new Association();
    clone.setValues(this);
    return clone;
  }

  public void setValues(Association otherElement) {
    super.setValues(otherElement);
    setSourceRef(otherElement.getSourceRef());
    setTargetRef(otherElement.getTargetRef());

    if (otherElement.getAssociationDirection() != null) {
      setAssociationDirection(otherElement.getAssociationDirection());
    }
  }
}
