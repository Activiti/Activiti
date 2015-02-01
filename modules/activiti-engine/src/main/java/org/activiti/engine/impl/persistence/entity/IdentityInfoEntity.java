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
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;


/**
 * @author Tom Baeyens
 */
public class IdentityInfoEntity implements PersistentObject, HasRevision, Serializable {
  
  private static final long serialVersionUID = 1L;
  
  public static final String TYPE_USERINFO = "userinfo";
  
  protected String id;
  protected int revision;
  protected String type;
  protected String userId;
  protected String key;
  protected String value;
  protected String password;
  protected byte[] passwordBytes;
  protected String parentId;
  protected Map<String, String> details;

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("value", value);
    persistentState.put("password", passwordBytes);
    return persistentState;
  }
  
  public int getRevisionNext() {
    return revision+1;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getUserId() {
    return userId;
  }
  
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public byte[] getPasswordBytes() {
    return passwordBytes;
  }

  public void setPasswordBytes(byte[] passwordBytes) {
    this.passwordBytes = passwordBytes;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return key;
  }

  public String getUsername() {
    return value;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }
  
  public Map<String, String> getDetails() {
    return details;
  }
  
  public void setDetails(Map<String, String> details) {
    this.details = details;
  }
}
