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
package org.activiti.engine.impl.task;

import java.util.HashSet;
import java.util.Set;


/**
 * Container for task definition information gathered at parsing time.
 * 
 * @author Joram Barrez
 */
public class TaskDefinition {
  
  protected String name;
  protected String description;
  protected String assignee;
  protected Set<String> candidateUserIds = new HashSet<String>();
  protected Set<String> candidateGroupIds = new HashSet<String>();
  protected String formResourceKey;
  
  // getters and setters //////////////////////////////////////////////////////

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }
  
  public void addCandidateGroupId(String groupId) {
    candidateGroupIds.add(groupId);
  }

  public Set<String> getCandidateGroupIds() {
    return candidateGroupIds;
  }
  
  public void addCandidateUserId(String userId) {
    candidateUserIds.add(userId);
  }
  
  public Set<String> getCandidateUserIds() {
    return candidateUserIds;
  }

  public String getFormResourceKey() {
    return formResourceKey;
  }

  public void setFormResourceKey(String formResourceKey) {
    this.formResourceKey = formResourceKey;
  }
}
