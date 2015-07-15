/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.Arrays;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.apache.commons.lang3.ObjectUtils;

/**
 * @author Tom Baeyens
 * @author Marcus Klimstra (CGI)
 */
public class ByteArrayEntity implements Serializable, PersistentObject, HasRevision {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;
  protected String name;
  protected byte[] bytes;
  protected String deploymentId;

  // Default constructor for SQL mapping
  protected ByteArrayEntity() {
  }

  public ByteArrayEntity(String name, byte[] bytes) {
    this.name = name;
    this.bytes = bytes;
  }

  public ByteArrayEntity(byte[] bytes) {
    this.bytes = bytes;
  }

  public static ByteArrayEntity createAndInsert(String name, byte[] bytes) {
    ByteArrayEntity byteArrayEntity = new ByteArrayEntity(name, bytes);

    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(byteArrayEntity);
  
    return byteArrayEntity;
  }
  
  public static ByteArrayEntity createAndInsert(byte[] bytes) {
    return createAndInsert(null, bytes);
  }
  
  public byte[] getBytes() {
    return bytes;
  }

  public Object getPersistentState() {
    return new PersistentState(name, bytes);
  }
  
  public int getRevisionNext() {
    return revision+1;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getDeploymentId() {
    return deploymentId;
  }
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }
  
  @Override
  public String toString() {
    return "ByteArrayEntity[id=" + id + ", name=" + name + ", size=" + (bytes != null ? bytes.length : 0) + "]";
  }

  // Wrapper for a byte array, needed to do byte array comparisons
  // See https://activiti.atlassian.net/browse/ACT-1524
  private static class PersistentState {
    
    private final String name;
    private final byte[] bytes;
    
    public PersistentState(String name, byte[] bytes) {
      this.name = name;
      this.bytes = bytes;
    }
    
    public boolean equals(Object obj) {
      if (obj instanceof PersistentState) {
        PersistentState other = (PersistentState) obj;
        return ObjectUtils.equals(this.name, other.name)
            && Arrays.equals(this.bytes, other.bytes);
      }
      return false;
    }
    
    @Override
    public int hashCode() {
      throw new UnsupportedOperationException();
    }
    
  }
  
}
