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

package org.activiti.rest.service.api.legacy.identity;

import org.activiti.engine.identity.Group;

/**
 * @author Ernesto Revilla
 */
public class LegacyGroupInfo {
  
  String id;
  String name;
  String type;
  
  public LegacyGroupInfo(){}
  
  public LegacyGroupInfo(Group group) {
    setId(group.getId());
    setName(group.getName());
    setType(group.getType());
  }
  
  public String getId() {
    return id;
  }
  public LegacyGroupInfo setId(String id) {
    this.id = id;
    return this;
  }
  public String getName() {
    return name;
  }
  public LegacyGroupInfo setName(String name) {
    this.name = name;
    return this;
  }
  public String getType() {
    return type;
  }
  public LegacyGroupInfo setType(String type) {
    this.type = type;
    return this;
  }
}
