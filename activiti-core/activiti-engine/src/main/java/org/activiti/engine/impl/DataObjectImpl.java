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

package org.activiti.engine.impl;

import org.activiti.engine.runtime.DataObject;

public class DataObjectImpl implements DataObject {
  private String name;
  private Object value;
  private String description;
  private String localizedName;
  private String localizedDescription;
  private String dataObjectDefinitionKey;

  private String type;

  public DataObjectImpl(String name, Object value, String description, String type, String localizedName,
      String localizedDescription, String dataObjectDefinitionKey) {

    this.name = name;
    this.value = value;
    this.type = type;
    this.description = description;
    this.localizedName = localizedName;
    this.localizedDescription = localizedDescription;
    this.dataObjectDefinitionKey = dataObjectDefinitionKey;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocalizedName() {
    if (localizedName != null && localizedName.length() > 0) {
      return localizedName;
    } else {
      return name;
    }
  }

  public void setLocalizedName(String localizedName) {
    this.localizedName = localizedName;
  }

  public String getDescription() {
    if (localizedDescription != null && localizedDescription.length() > 0) {
      return localizedDescription;
    } else {
      return description;
    }
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  public String getDataObjectDefinitionKey() {
    return dataObjectDefinitionKey;
  }


  public void setDataObjectDefinitionKey(String dataObjectDefinitionKey) {
    this.dataObjectDefinitionKey = dataObjectDefinitionKey;
  }
}
