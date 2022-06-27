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

public class ExtensionAttribute {

  protected String name;
  protected String value;
  protected String namespacePrefix;
  protected String namespace;

  public ExtensionAttribute() {
  }

  public ExtensionAttribute(String name) {
    this.name = name;
  }

  public ExtensionAttribute(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getNamespacePrefix() {
    return namespacePrefix;
  }

  public void setNamespacePrefix(String namespacePrefix) {
    this.namespacePrefix = namespacePrefix;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (namespacePrefix != null) {
      sb.append(namespacePrefix);
      if (name != null)
        sb.append(":").append(name);
    } else
      sb.append(name);
    if (value != null)
      sb.append("=").append(value);
    return sb.toString();
  }

  public ExtensionAttribute clone() {
    ExtensionAttribute clone = new ExtensionAttribute();
    clone.setValues(this);
    return clone;
  }

  public void setValues(ExtensionAttribute otherAttribute) {
    setName(otherAttribute.getName());
    setValue(otherAttribute.getValue());
    setNamespacePrefix(otherAttribute.getNamespacePrefix());
    setNamespace(otherAttribute.getNamespace());
  }
}
