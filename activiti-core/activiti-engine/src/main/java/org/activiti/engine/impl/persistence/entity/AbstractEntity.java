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

package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.impl.db.HasRevision;

/**
 * Abstract superclass for the common properties of all {@link Entity} implementations.
 *

 */
public abstract class AbstractEntity implements Entity, HasRevision {

  protected String id;
  protected int revision = 1;

  protected boolean isInserted;
  protected boolean isUpdated;
  protected boolean isDeleted;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public boolean isInserted() {
    return isInserted;
  }

  public void setInserted(boolean isInserted) {
    this.isInserted = isInserted;
  }

  public boolean isUpdated() {
    return isUpdated;
  }

  public void setUpdated(boolean isUpdated) {
    this.isUpdated = isUpdated;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

}
