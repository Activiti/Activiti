package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.MimeType;

public abstract class AbstractMimeType implements MimeType {

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MimeType other = (MimeType) obj;
    if (getName() == null) {
      if (other.getName() != null)
        return false;
    } else if (!getName().equals(other.getName()))
      return false;
    return true;
  }
  
  @Override
  public String toString() {
    return getClass().getName();    
  }

}
