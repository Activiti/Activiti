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
package org.activiti.spring.test.components.scope;

import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * dumb object to demonstrate holding scoped state for the duration of a business process
 *

 */
public class StatefulObject implements Serializable, InitializingBean {

  private transient Logger logger = LoggerFactory.getLogger(getClass());

  public static final long serialVersionUID = 1L;

  private String name;
  private int visitedCount = 0;

  private long customerId;

  public long getCustomerId() {
    return customerId;
  }

  @Value("#{processInstance}")
  transient ProcessInstance processInstance;

  @Value("#{executionId}")
  String executionId;

  @Value("#{processVariables['customerId']}")
  public void setCustomerId(long customerId) {

    this.customerId = customerId;

    logger.info("setting this {} instances 'customerId' to {}. The current executionId is {}", StatefulObject.class.getName(), this.customerId, this.executionId);

  }

  public StatefulObject() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    StatefulObject that = (StatefulObject) o;

    if (visitedCount != that.visitedCount)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + visitedCount;
    return result;
  }

  @Override
  public String toString() {
    return "StatefulObject{" + "name='" + name + '\'' + ", visitedCount=" + visitedCount + '}';
  }

  public void increment() {
    this.visitedCount += 1;
  }

  public int getVisitedCount() {
    return this.visitedCount;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(this.processInstance, "the processInstance should be equal to the currently active processInstance!");
    logger.info("the 'processInstance' property is non-null: PI ID#{}", this.processInstance.getId());
  }
}
