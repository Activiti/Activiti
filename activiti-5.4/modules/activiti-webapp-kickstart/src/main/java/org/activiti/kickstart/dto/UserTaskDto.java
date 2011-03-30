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
package org.activiti.kickstart.dto;

import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.FormalExpression;
import org.activiti.kickstart.bpmn20.model.activity.resource.HumanPerformer;
import org.activiti.kickstart.bpmn20.model.activity.resource.PotentialOwner;
import org.activiti.kickstart.bpmn20.model.activity.resource.ResourceAssignmentExpression;
import org.activiti.kickstart.bpmn20.model.activity.type.UserTask;

/**
 * @author Joram Barrez
 */
public class UserTaskDto extends BaseTaskDto {

  protected String assignee;

  protected String groups;

  protected FormDto form;

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getGroups() {
    return groups;
  }

  public void setGroups(String groups) {
    this.groups = groups;
  }

  public FormDto getForm() {
    return form;
  }

  public void setForm(FormDto formDto) {
    this.form = formDto;
  }

  public String generateDefaultFormName() {
    return name.replace(" ", "_") + ".form";
  }

  @Override
  public FlowElement createFlowElement() {
    UserTask userTask = new UserTask();
    if (getForm() != null) {
      userTask.setFormKey(generateDefaultFormName());
    }

    // assignee
    if (getAssignee() != null && !"".equals(getAssignee())) {
      HumanPerformer humanPerformer = new HumanPerformer();
      humanPerformer.setId(userTask.getId() + "_humanPerformer");
      ResourceAssignmentExpression assignmentExpression = new ResourceAssignmentExpression();
      assignmentExpression.setId(userTask.getId() + "_humanPerformer_assignmentExpression");
      FormalExpression formalExpression = new FormalExpression(getAssignee());
      formalExpression.setId(userTask.getId() + "_humanPerformer_formalExpressions");
      assignmentExpression.setExpression(formalExpression);
      humanPerformer.setResourceAssignmentExpression(assignmentExpression);
      userTask.getActivityResource().add(humanPerformer);
    }

    // groups
    if (getGroups() != null && !"".equals(getGroups())) {
      PotentialOwner potentialOwner = new PotentialOwner();
      potentialOwner.setId(userTask.getId() + "_potentialOwner");
      ResourceAssignmentExpression assignmentExpression = new ResourceAssignmentExpression();
      assignmentExpression.setId(userTask.getId() + "_potentialOwner_assignmentExpression");

      StringBuilder groups = new StringBuilder();
      for (String group : getGroups().split(",")) {
        groups.append(group + ",");
      }
      groups.deleteCharAt(groups.length() - 1);
      FormalExpression formalExpression = new FormalExpression(groups.toString());

      formalExpression.setId(userTask.getId() + "_potentialOwner_formalExpressions");
      assignmentExpression.setExpression(formalExpression);
      potentialOwner.setResourceAssignmentExpression(assignmentExpression);
      userTask.getActivityResource().add(potentialOwner);
    }
    return userTask;
  }

}
