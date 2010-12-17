package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.MimeType;

public abstract class AbstractMimeType implements MimeType {

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getContentType() == null) ? 0 : getContentType().hashCode());
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
    if (getContentType() == null) {
      if (other.getContentType() != null)
        return false;
    } else if (!getContentType().equals(other.getContentType()))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

}
