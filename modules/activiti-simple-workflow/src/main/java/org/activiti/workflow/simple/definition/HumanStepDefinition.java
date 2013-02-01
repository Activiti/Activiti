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
package org.activiti.workflow.simple.definition;

import java.util.List;

/**
 * Defines a step that must be executed by a human actor.
 * 
 * @author Joram Barrez
 */
public class HumanStepDefinition extends AbstractNamedStepDefinition {

  protected String assignee;
  protected boolean assigneeIsInitiator = false;
  protected List<String> candidateUsers;
  protected List<String> candidateGroups;
  protected FormDefinition form;

  public boolean isAssigneeInitiator() {
    return assigneeIsInitiator;
  }

  public void setAssigneeIsInitiator(boolean assigneeIsInitiator) {
    this.assigneeIsInitiator = assigneeIsInitiator;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public List<String> getCandidateUsers() {
    return candidateUsers;
  }

  public void setCandidateUsers(List<String> candidateUsers) {
    this.candidateUsers = candidateUsers;
  }

  public List<String> getCandidateGroups() {
    return candidateGroups;
  }
  
  public String getCandidateGroupsCommaSeparated() {
    if (candidateGroups == null || candidateGroups.size() == 0) {
      return null;
    }
    
    StringBuilder strb = new StringBuilder();
    for (String group : candidateGroups) {
      strb.append(group + ", ");
    }
    strb.delete(strb.length() - 2, strb.length());
    return strb.toString();
  }

  public void setCandidateGroups(List<String> candidateGroups) {
    this.candidateGroups = candidateGroups;
  }

  public FormDefinition getForm() {
    return form;
  }

  public void setForm(FormDefinition form) {
    this.form = form;
  }
}
