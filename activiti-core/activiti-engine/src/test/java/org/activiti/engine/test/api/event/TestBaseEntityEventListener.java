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

package org.activiti.engine.test.api.event;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.BaseEntityEventListener;

public class TestBaseEntityEventListener extends BaseEntityEventListener {

  private boolean updateReceived;
  private boolean createReceived;
  private boolean deleteReceived;
  private boolean initializeReceived;
  private boolean customReceived;

  public TestBaseEntityEventListener() {
    super();
  }

  public TestBaseEntityEventListener(Class<?> entityClass) {
    super(false, entityClass);
  }

  public void reset() {
    updateReceived = false;
    createReceived = false;
    deleteReceived = false;
    customReceived = false;
    initializeReceived = false;
  }

  public boolean isCreateReceived() {
    return createReceived;
  }

  public boolean isCustomReceived() {
    return customReceived;
  }

  public boolean isDeleteReceived() {
    return deleteReceived;
  }

  public boolean isUpdateReceived() {
    return updateReceived;
  }

  public boolean isInitializeReceived() {
    return initializeReceived;
  }

  @Override
  protected void onCreate(ActivitiEvent event) {
    createReceived = true;
  }

  @Override
  protected void onDelete(ActivitiEvent event) {
    deleteReceived = true;
  }

  @Override
  protected void onUpdate(ActivitiEvent event) {
    updateReceived = true;
  }

  @Override
  protected void onEntityEvent(ActivitiEvent event) {
    customReceived = true;
  }

  @Override
  protected void onInitialized(ActivitiEvent event) {
    initializeReceived = true;
  }
}
