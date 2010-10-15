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
package org.activiti.engine.impl.runtime;

import java.io.Serializable;

import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.history.HistoricVariableUpdateEntity;
import org.activiti.engine.impl.variable.ByteArrayType;

/**
 * @author Tom Baeyens
 */
public class ByteArrayEntity implements Serializable, PersistentObject {

  private static final long serialVersionUID = 1L;

  private static final Object PERSISTENTSTATE_NULL = new Object();

  protected String id;
  protected int revision;
  protected String name;
  protected byte[] bytes;
  protected String deploymentId;
  protected ByteArrayType variable;

  public ByteArrayEntity() {
  }

  public ByteArrayEntity(String name, byte[] bytes) {
    this.name = name;
    this.bytes = bytes;
  }

  public ByteArrayEntity(ByteArrayType variable, byte[] bytes) {
    this.variable = variable;
    this.bytes = bytes;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public Object getPersistentState() {
    return (bytes != null ? bytes : PERSISTENTSTATE_NULL);
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
}
