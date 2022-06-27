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

package org.activiti.engine.impl.bpmn.data;

/**
 * An instance of {@link ItemDefinition}
 *

 */
public class ItemInstance {

  protected ItemDefinition item;

  protected StructureInstance structureInstance;

  public ItemInstance(ItemDefinition item, StructureInstance structureInstance) {
    this.item = item;
    this.structureInstance = structureInstance;
  }

  public ItemDefinition getItem() {
    return this.item;
  }

  public StructureInstance getStructureInstance() {
    return this.structureInstance;
  }

  private FieldBaseStructureInstance getFieldBaseStructureInstance() {
    return (FieldBaseStructureInstance) this.structureInstance;
  }

  public Object getFieldValue(String fieldName) {
    return this.getFieldBaseStructureInstance().getFieldValue(fieldName);
  }

  public void setFieldValue(String fieldName, Object value) {
    this.getFieldBaseStructureInstance().setFieldValue(fieldName, value);
  }
}
