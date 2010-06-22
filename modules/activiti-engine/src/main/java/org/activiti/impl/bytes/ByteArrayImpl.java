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
package org.activiti.impl.bytes;

import java.io.Serializable;

import org.activiti.impl.persistence.PersistentObject;
import org.activiti.impl.variable.ByteArrayType;


/**
 * @author Tom Baeyens
 */
public class ByteArrayImpl implements Serializable, PersistentObject {
	
  private static final long serialVersionUID = 1L;
  
  private static final Object PERSISTENTSTATE_NULL = new Object();
  
  protected String id;

  protected String name;
  
  protected byte[] bytes;

  protected String deploymentId;

  protected ByteArrayType variable;

  public ByteArrayImpl() {
  }

  public ByteArrayImpl(String name, byte[] bytes) {
    this.name = name;
    this.bytes = bytes;
  }

  public ByteArrayImpl(ByteArrayType variable, byte[] bytes) {
    this.variable = variable;
    this.bytes = bytes;
  }

  public byte[] getBytes() {
//    // the bytes are lazy initialized
//    if (bytes == null) {
//      bytes = CommandContext
//        .getCurrent()
//        .getPersistenceSession()
//        .getByteArrayBytes(id);
//    }
    return bytes;
  }
  
  public Object getPersistentState() {
    return (bytes!=null ? bytes : PERSISTENTSTATE_NULL);
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
}
