package org.activiti.form.engine.impl.persistence.entity;

import java.io.Serializable;

import org.activiti.form.engine.impl.context.Context;

/**
 * <p>
 * Encapsulates the logic for transparently working with {@link ByteArrayEntity} .
 * </p>
 * 
 * @author Marcus Klimstra (CGI)
 * @author Tijs Rademakers
 */
public class ResourceRef implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private String name;
  private ResourceEntity entity;
  protected boolean deleted;

  public ResourceRef() {
  }

  // Only intended to be used by ByteArrayRefTypeHandler
  public ResourceRef(String id) {
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
        ResourceEntityManager resourceEntityManager = Context.getCommandContext().getResourceEntityManager();
        entity = resourceEntityManager.create();
        entity.setName(name);
        entity.setBytes(bytes);
        resourceEntityManager.insert(entity);
        id = entity.getId();
      }
    } else {
      ensureInitialized();
      entity.setBytes(bytes);
    }
  }

  public ResourceEntity getEntity() {
    ensureInitialized();
    return entity;
  }

  public void delete() {
    if (!deleted && id != null) {
      if (entity != null) {
        // if the entity has been loaded already,
        // we might as well use the safer optimistic locking delete.
        Context.getCommandContext().getResourceEntityManager().delete(entity);
      } else {
        Context.getCommandContext().getResourceEntityManager().delete(id);
      }
      entity = null;
      id = null;
      deleted = true;
    }
  }

  private void ensureInitialized() {
    if (id != null && entity == null) {
      entity = Context.getCommandContext().getResourceEntityManager().findById(id);
      name = entity.getName();
    }
  }

  public boolean isDeleted() {
    return deleted;
  }

  @Override
  public String toString() {
    return "ResourceRef[id=" + id + ", name=" + name + ", entity=" + entity + (deleted ? ", deleted]" : "]");
  }
}
