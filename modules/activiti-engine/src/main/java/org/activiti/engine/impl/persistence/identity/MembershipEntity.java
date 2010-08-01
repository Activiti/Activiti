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

import org.activiti.engine.impl.persistence.PersistentObject;


/**
 * @author Tom Baeyens
 */
public class MembershipEntity implements Serializable, PersistentObject {

  private static final long serialVersionUID = 1L;

  protected UserEntity user;
  protected GroupEntity group;

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

  public UserEntity getUser() {
    return user;
  }
  public void setUser(UserEntity user) {
    this.user = user;
  }
  public GroupEntity getGroup() {
    return group;
  }
  public void setGroup(GroupEntity group) {
    this.group = group;
  }
}
