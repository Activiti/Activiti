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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.util.ReflectUtil;

/**
 * An {@link ActivitiEventListener} implementation which uses a classname to create a delegate {@link ActivitiEventListener} instance to use for event notification. <br>
 * <br>
 *
 * In case an entityClass was passed in the constructor, only events that are {@link ActivitiEntityEvent}'s that target an entity of the given type, are dispatched to the delegate.
 *

 */
public class DelegateActivitiEventListener extends BaseDelegateEventListener {

  protected String className;
  protected ActivitiEventListener delegateInstance;
  protected boolean failOnException = false;

  public DelegateActivitiEventListener(String className, Class<?> entityClass) {
    this.className = className;
    setEntityClass(entityClass);
  }

  @Override
  public void onEvent(ActivitiEvent event) {
    if (isValidEvent(event)) {
      getDelegateInstance().onEvent(event);
    }
  }

  @Override
  public boolean isFailOnException() {
    if (delegateInstance != null) {
      return delegateInstance.isFailOnException();
    }
    return failOnException;
  }

  protected ActivitiEventListener getDelegateInstance() {
    if (delegateInstance == null) {
      Object instance = ReflectUtil.instantiate(className);
      if (instance instanceof ActivitiEventListener) {
        delegateInstance = (ActivitiEventListener) instance;
      } else {
        // Force failing of the listener invocation, since the delegate
        // cannot be created
        failOnException = true;
        throw new ActivitiIllegalArgumentException("Class " + className + " does not implement " + ActivitiEventListener.class.getName());
      }
    }
    return delegateInstance;
  }
}
