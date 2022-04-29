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

public class ItemDefinition extends BaseElement {

  protected String structureRef;
  protected String itemKind;

  public String getStructureRef() {
    return structureRef;
  }

  public void setStructureRef(String structureRef) {
    this.structureRef = structureRef;
  }

  public String getItemKind() {
    return itemKind;
  }

  public void setItemKind(String itemKind) {
    this.itemKind = itemKind;
  }

  public ItemDefinition clone() {
    ItemDefinition clone = new ItemDefinition();
    clone.setValues(this);
    return clone;
  }

  public void setValues(ItemDefinition otherElement) {
    super.setValues(otherElement);
    setStructureRef(otherElement.getStructureRef());
    setItemKind(otherElement.getItemKind());
  }
}
