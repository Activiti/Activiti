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

public class DataSpec extends BaseElement {

  protected String name;
  protected String itemSubjectRef;
  protected boolean isCollection;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getItemSubjectRef() {
    return itemSubjectRef;
  }

  public void setItemSubjectRef(String itemSubjectRef) {
    this.itemSubjectRef = itemSubjectRef;
  }

  public boolean isCollection() {
    return isCollection;
  }

  public void setCollection(boolean isCollection) {
    this.isCollection = isCollection;
  }

  public DataSpec clone() {
    DataSpec clone = new DataSpec();
    clone.setValues(this);
    return clone;
  }

  public void setValues(DataSpec otherDataSpec) {
    setName(otherDataSpec.getName());
    setItemSubjectRef(otherDataSpec.getItemSubjectRef());
    setCollection(otherDataSpec.isCollection());
  }
}
