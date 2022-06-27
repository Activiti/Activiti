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

public class Import extends BaseElement {

  protected String importType;
  protected String location;
  protected String namespace;

  public String getImportType() {
    return importType;
  }

  public void setImportType(String importType) {
    this.importType = importType;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public Import clone() {
    Import clone = new Import();
    clone.setValues(this);
    return clone;
  }

  public void setValues(Import otherElement) {
    super.setValues(otherElement);
    setImportType(otherElement.getImportType());
    setLocation(otherElement.getLocation());
    setNamespace(otherElement.getNamespace());
  }
}
