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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ActivitiListener extends BaseElement {

  protected String event;
  protected String implementationType;
  protected String implementation;
  protected List<FieldExtension> fieldExtensions = new ArrayList<FieldExtension>();
  protected String onTransaction;
  protected String customPropertiesResolverImplementationType;
  protected String customPropertiesResolverImplementation;

  @JsonIgnore
  protected Object instance; // Can be used to set an instance of the listener directly. That instance will then always be reused.

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public String getImplementationType() {
    return implementationType;
  }

  public void setImplementationType(String implementationType) {
    this.implementationType = implementationType;
  }

  public String getImplementation() {
    return implementation;
  }

  public void setImplementation(String implementation) {
    this.implementation = implementation;
  }

  public List<FieldExtension> getFieldExtensions() {
    return fieldExtensions;
  }

  public void setFieldExtensions(List<FieldExtension> fieldExtensions) {
    this.fieldExtensions = fieldExtensions;
  }

  public String getOnTransaction() {
    return onTransaction;
  }

  public void setOnTransaction(String onTransaction) {
    this.onTransaction = onTransaction;
  }

  public String getCustomPropertiesResolverImplementationType() {
    return customPropertiesResolverImplementationType;
  }

  public void setCustomPropertiesResolverImplementationType(String customPropertiesResolverImplementationType) {
    this.customPropertiesResolverImplementationType = customPropertiesResolverImplementationType;
  }

  public String getCustomPropertiesResolverImplementation() {
    return customPropertiesResolverImplementation;
  }

  public void setCustomPropertiesResolverImplementation(String customPropertiesResolverImplementation) {
    this.customPropertiesResolverImplementation = customPropertiesResolverImplementation;
  }

  public Object getInstance() {
    return instance;
  }

  public void setInstance(Object instance) {
    this.instance = instance;
  }

  public ActivitiListener clone() {
    ActivitiListener clone = new ActivitiListener();
    clone.setValues(this);
    return clone;
  }

  public void setValues(ActivitiListener otherListener) {
    setEvent(otherListener.getEvent());
    setImplementation(otherListener.getImplementation());
    setImplementationType(otherListener.getImplementationType());

    fieldExtensions = new ArrayList<FieldExtension>();
    if (otherListener.getFieldExtensions() != null && !otherListener.getFieldExtensions().isEmpty()) {
      for (FieldExtension extension : otherListener.getFieldExtensions()) {
        fieldExtensions.add(extension.clone());
      }
    }
  }
}
