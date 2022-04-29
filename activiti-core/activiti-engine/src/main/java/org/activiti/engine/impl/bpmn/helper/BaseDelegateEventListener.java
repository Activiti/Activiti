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

package org.activiti.engine.impl.bpmn.helper;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;

/**
 * Base implementation of a {@link ActivitiEventListener}, used when creating event-listeners that are part of a BPMN definition.
 *

 */
public abstract class BaseDelegateEventListener implements ActivitiEventListener {

  protected Class<?> entityClass;

  public void setEntityClass(Class<?> entityClass) {
    this.entityClass = entityClass;
  }

  protected boolean isValidEvent(ActivitiEvent event) {
    boolean valid = false;
    if (entityClass != null) {
      if (event instanceof ActivitiEntityEvent) {
        Object entity = ((ActivitiEntityEvent) event).getEntity();
        if (entity != null) {
          valid = entityClass.isAssignableFrom(entity.getClass());
        }
      }
    } else {
      // If no class is specified, all events are valid
      valid = true;
    }
    return valid;
  }

}
