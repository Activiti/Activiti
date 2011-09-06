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
package org.activiti.cdi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.activiti.engine.IdentityService;


/**
 * Holds the id and groups of the current actor.
 * If this is set, activiti-cdi automatically executes all activiti 
 * commands on behalf of the current actor.
 * 
 * @see IdentityService#setAuthenticatedUserId(String)
 * 
 * @author Daniel Meyer
 */
@Named
@SessionScoped
public class Actor implements Serializable {

  private static final long serialVersionUID = 1L;

  private String actorId;

  private Set<String> groups = new HashSet<String>();

  public Actor() {
  }

  public Actor(String id) {
    actorId = id;
  }

  public String getActorId() {
    return actorId;
  }

  public void setActorId(String actorId) {
    this.actorId = actorId;
  }

  public List<String> getGroups() {
    return new ArrayList<String>(groups);
  }

  public void addGroup(String groupId) {
    groups.add(groupId);
  }

  public void removeGroup(String groupId) {
    groups.remove(groupId);
  }

  public void resetGroups() {
    groups.clear();
  }
}
