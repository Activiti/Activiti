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
package org.activiti.engine.impl.persistence.identity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.PersistentObject;


/**
 * @author Tom Baeyens
 */
public class GroupEntity implements Group, Serializable, PersistentObject {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String name;
  protected String type;
  
  protected boolean isNew = false;

  public GroupEntity() {
  }
  
  public GroupEntity(String id) {
    this.id = id;
    this.isNew = true;
  }
  
  public void update(GroupEntity group) {
    this.name = group.getName();
    this.type = group.getType();
  }
  
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", name);
    persistentState.put("type", type);
    return persistentState;
  }

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
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public boolean isNew() {
    return isNew;
  }  
}
