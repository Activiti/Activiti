package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;

import org.activiti.engine.impl.context.Context;

/**
 * <p>Encapsulates the logic for transparently working with {@link ByteArrayEntity}.</p>
 * 
 * <p>Make sure that instance variables (i.e. fields) of this type are always initialized, 
 * and thus <strong>never</strong> null.</p>
 * 
 * <p>For example:</p>
 * <pre>
 * private final ByteArrayRef byteArrayRef = new ByteArrayRef();
 * </pre>
 * 
 * @author Marcus Klimstra (CGI)
 */
public final class ByteArrayRef implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  private String id;
  private String name;
  private ByteArrayEntity entity;
  protected boolean deleted = false;

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
        entity = ByteArrayEntity.createAndInsert(name, bytes);
        id = entity.getId();
      }
    }
    else {
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
        Context.getCommandContext()
        .getByteArrayEntityManager()
        .deleteByteArray(entity);
      }
      else {
        Context.getCommandContext()
          .getByteArrayEntityManager()
          .deleteByteArrayById(id);
      }
      entity = null;
      id = null;
      deleted = true;
    }
  }
  
  private void ensureInitialized() {
    if (id != null && entity == null) {
      entity = Context.getCommandContext()
        .getByteArrayEntityManager()
        .findById(id);
      name = entity.getName();
    }
  }

  public boolean isDeleted() {
    return deleted;
  }

  @Override
  public String toString() {
    return "ByteArrayRef[id=" + id + ", name=" + name + ", entity=" + entity + (deleted ? ", deleted]" :"]");
  }
}
