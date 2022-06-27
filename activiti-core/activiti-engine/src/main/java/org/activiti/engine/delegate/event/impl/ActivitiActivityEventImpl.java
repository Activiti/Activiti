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

package org.activiti.engine.delegate.event.impl;

import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;

/**
 * Implementation of an {@link ActivitiActivityEvent}.
 *


 */
public class ActivitiActivityEventImpl extends ActivitiEventImpl implements ActivitiActivityEvent {

  protected String activityId;
  protected String activityName;
  protected String activityType;
  protected String behaviorClass;

  public ActivitiActivityEventImpl(ActivitiEventType type) {
    super(type);
  }

  @Override
  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getActivityName() {
    return activityName;
  }

  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }

  @Override
  public String getActivityType() {
    return activityType;
  }

  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  public String getBehaviorClass() {
    return behaviorClass;
  }

  public void setBehaviorClass(String behaviorClass) {
    this.behaviorClass = behaviorClass;
  }

}
