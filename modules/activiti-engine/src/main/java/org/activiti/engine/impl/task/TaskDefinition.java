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

import org.activiti.engine.impl.el.ActivitiValueExpression;

/**
 * Container for task definition information gathered at parsing time.
 * 
 * @author Joram Barrez
 */
public class TaskDefinition {

  protected ActivitiValueExpression nameValueExpression;
  protected ActivitiValueExpression descriptionValueExpression;
  protected ActivitiValueExpression assigneeValueExpression;
  protected Set<ActivitiValueExpression> candidateUserIdValueExpressions = new HashSet<ActivitiValueExpression>();
  protected Set<ActivitiValueExpression> candidateGroupIdValueExpressions = new HashSet<ActivitiValueExpression>();
  protected String formResourceKey;

  // getters and setters //////////////////////////////////////////////////////

  public ActivitiValueExpression getNameValueExpression() {
    return nameValueExpression;
  }

  public void setNameValueExpression(ActivitiValueExpression nameValueExpression) {
    this.nameValueExpression = nameValueExpression;
  }

  public ActivitiValueExpression getDescriptionValueExpression() {
    return descriptionValueExpression;
  }

  public void setDescriptionValueExpression(ActivitiValueExpression descriptionValueExpression) {
    this.descriptionValueExpression = descriptionValueExpression;
  }

  public ActivitiValueExpression getAssigneeValueExpression() {
    return assigneeValueExpression;
  }

  public void setAssigneeValueExpression(ActivitiValueExpression assigneeValueExpression) {
    this.assigneeValueExpression = assigneeValueExpression;
  }

  public Set<ActivitiValueExpression> getCandidateUserIdValueExpressions() {
    return candidateUserIdValueExpressions;
  }

  public void addCandidateUserIdValueExpression(ActivitiValueExpression userId) {
    candidateUserIdValueExpressions.add(userId);
  }

  public Set<ActivitiValueExpression> getCandidateGroupIdValueExpressions() {
    return candidateGroupIdValueExpressions;
  }

  public void addCandidateGroupIdValueExpression(ActivitiValueExpression groupId) {
    candidateGroupIdValueExpressions.add(groupId);
  }

  public String getFormResourceKey() {
    return formResourceKey;
  }

  public void setFormResourceKey(String formResourceKey) {
    this.formResourceKey = formResourceKey;
  }

}
