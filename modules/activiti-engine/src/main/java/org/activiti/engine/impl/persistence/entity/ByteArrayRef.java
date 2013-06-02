package org.activiti.engine.impl.persistence.entity;

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
 * @author Marcus Klimstra
 */
public final class ByteArrayRef {
  private String id;
  private String name;
  private ByteArrayEntity entity;
  
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
    if (id != null) {
      Context.getCommandContext()
        .getByteArrayEntityManager()
        .deleteByteArray(getEntity());
//        .deleteByteArrayById(id);
      id = null;
      entity = null;
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

  @Override
  public String toString() {
    return "ByteArrayRef[id=" + id + ", name=" + name + ", entity=" + entity + "]";
  }
}
