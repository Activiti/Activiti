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

import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEntityWithVariablesEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;

/**
 * Base class for all {@link ActivitiEntityEvent} implementations, related to entities with variables.
 *

 */
@SuppressWarnings("rawtypes")
public class ActivitiEntityWithVariablesEventImpl extends ActivitiEntityEventImpl implements ActivitiEntityWithVariablesEvent {

  protected Map variables;
  protected boolean localScope;

  public ActivitiEntityWithVariablesEventImpl(Object entity, Map variables, boolean localScope, ActivitiEventType type) {
    super(entity, type);

    this.variables = variables;
    this.localScope = localScope;
  }

  @Override
  public Map getVariables() {
    return variables;
  }

  @Override
  public boolean isLocalScope() {
    return localScope;
  }
}
