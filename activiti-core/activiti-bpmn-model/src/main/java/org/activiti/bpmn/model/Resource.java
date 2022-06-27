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

/**
 * The Resource class is used to specify resources that can be referenced by
 * Activities. These Resources can be Human Resources as well as any other
 * resource assigned to Activities during Process execution time.
 *
 */
public class Resource extends BaseElement {

  protected String name;

  public Resource(String resourceId, String resourceName) {
    super();
    setId(resourceId);
    setName(resourceName);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public BaseElement clone() {
    return new Resource(getId(), getName());
  }
}
