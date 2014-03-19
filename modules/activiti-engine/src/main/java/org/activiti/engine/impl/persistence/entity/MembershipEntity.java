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

import org.activiti.engine.impl.db.PersistentObject;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class MembershipEntity implements PersistentObject, Serializable {

  private static final long serialVersionUID = 1L;

  protected String userId;
  protected String groupId;
  
  public Object getPersistentState() {
    // membership is not updatable
    return MembershipEntity.class;
  }
  public String getId() {
    // membership doesn't have an id
    return null;
  }
  public void setId(String id) {
    // membership doesn't have an id
  }
  
  public String getUserId() {
    return userId;
  }
  
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  public String getGroupId() {
    return groupId;
  }
  
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
  
}
