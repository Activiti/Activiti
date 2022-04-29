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


package org.activiti.engine.impl.bpmn.parser;

import java.io.Serializable;

/**
 * Represents a field declaration in object form:
 *
 * &lt;field name='someField&gt; &lt;string ...
 *


 */
public class FieldDeclaration implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected String type;
  protected Object value;

  public FieldDeclaration(String name, String type, Object value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }

  public FieldDeclaration() {

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

}
