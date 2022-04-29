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

import java.io.Serializable;

import org.activiti.engine.impl.context.Context;

/**
 * <p>
 * Encapsulates the logic for transparently working with {@link ByteArrayEntity} .
 * </p>
 *

 */
public class ByteArrayRef implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private String name;
  private ByteArrayEntity entity;
  protected boolean deleted;

  public ByteArrayRef() {
  }

  // Only intended to be used by ByteArrayRefTypeHandler
  public ByteArrayRef(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public byte[] getBytes() {
    ensureInitialized();
    return (entity != null ? entity.getBytes() : null);
  }

  public void setValue(String name, byte[] bytes) {
    this.name = name;
    setBytes(bytes);
  }

  private void setBytes(byte[] bytes) {
    if (id == null) {
      if (bytes != null) {
        ByteArrayEntityManager byteArrayEntityManager = Context.getCommandContext().getByteArrayEntityManager();
        entity = byteArrayEntityManager.create();
        entity.setName(name);
        entity.setBytes(bytes);
        byteArrayEntityManager.insert(entity);
        id = entity.getId();
      }
    } else {
      ensureInitialized();
      entity.setBytes(bytes);
    }
  }

  public ByteArrayEntity getEntity() {
    ensureInitialized();
    return entity;
  }

  public void delete() {
    if (!deleted && id != null) {
      if (entity != null) {
        // if the entity has been loaded already,
        // we might as well use the safer optimistic locking delete.
        Context.getCommandContext().getByteArrayEntityManager().delete(entity);
      } else {
        Context.getCommandContext().getByteArrayEntityManager().deleteByteArrayById(id);
      }
      entity = null;
      id = null;
      deleted = true;
    }
  }

  private void ensureInitialized() {
    if (id != null && entity == null) {
      entity = Context.getCommandContext().getByteArrayEntityManager().findById(id);
      name = entity.getName();
    }
  }

  public boolean isDeleted() {
    return deleted;
  }

  @Override
  public String toString() {
    return "ByteArrayRef[id=" + id + ", name=" + name + ", entity=" + entity + (deleted ? ", deleted]" : "]");
  }
}
