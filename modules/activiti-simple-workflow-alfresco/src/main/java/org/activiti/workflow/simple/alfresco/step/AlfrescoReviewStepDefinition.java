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
package org.activiti.workflow.simple.alfresco.step;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.workflow.simple.alfresco.conversion.exception.AlfrescoSimpleWorkflowException;
import org.activiti.workflow.simple.definition.AbstractStepDefinitionContainer;
import org.activiti.workflow.simple.definition.FormStepDefinition;
import org.activiti.workflow.simple.definition.HumanStepAssignment.HumanStepAssignmentType;
import org.activiti.workflow.simple.definition.NamedStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A step that assigns a review-task to one or multiple users. The user(s) can approve or reject and based
 * on the overall approval/reject count, the 'review' is considered approved or rejected. When approved, this step
 * continues to the next step defined in the process. When rejected, additional steps can be executes,
 * which are present in the rejectionSteps property.
 * 
 * @author Frederik Heremans 
 */
@JsonTypeName("review-step")
public class AlfrescoReviewStepDefinition extends AbstractStepDefinitionContainer<AlfrescoReviewStepDefinition> implements StepDefinition, NamedStepDefinition, FormStepDefinition {
  
  private static final long serialVersionUID = 1L;
  
  protected String assignmentPropertyName;
  protected HumanStepAssignmentType assignmentType = HumanStepAssignmentType.USER;
  protected FormDefinition form;
  protected String requiredApprovalCount;
  protected boolean endProcessOnReject = false;
  
  protected String name;
  protected String description;
  
  protected Map<String, Object> parameters = new HashMap<String, Object>();
  
  /**
   * @param assignmentType type of assignment this review-task has. Can be either {@link HumanStepAssignmentType#USER}
   * or {@link HumanStepAssignmentType#USERS}.
   */
  public void setAssignmentType(HumanStepAssignmentType assignmentType) {
  	if(assignmentType != HumanStepAssignmentType.USER && assignmentType != HumanStepAssignmentType.USERS) {
  		throw new AlfrescoSimpleWorkflowException("Review step can only be assigned to a single user or multiple users.");
  	}
	  this.assignmentType = assignmentType;
  }
  
  @JsonProperty("assignment-type")
  public HumanStepAssignmentType getAssignmentType() {
	  return assignmentType;
  }
  
  @Override
  public void setDescription(String description) {
  	this.description = description;
  }
  
  public String getDescription() {
	  return description;
  }
  
  @JsonProperty("assignment-property-name")
  public void setAssignmentPropertyName(String assignmentPropertyName) {
	  this.assignmentPropertyName = assignmentPropertyName;
  }

  public String getAssignmentPropertyName() {
	  return assignmentPropertyName;
  }
  
  public void setRequiredApprovalCount(String requiredApprovalCount) {
	  this.requiredApprovalCount = requiredApprovalCount;
  }
  
  @JsonProperty("required-count")
  public String getRequiredApprovalCount() {
	  return requiredApprovalCount;
  }
  
  public FormDefinition getForm() {
	  return form;
  }
  
  public void setForm(FormDefinition form) {
	  this.form = form;
  }
  
  public void setName(String name) {
	  this.name = name;
  }
  
  public String getName() {
	  return name;
  }
  
	@Override
	public StepDefinition clone() {
		AlfrescoReviewStepDefinition clone = new AlfrescoReviewStepDefinition();
		clone.setValues(this);
		return clone;
	}

	@Override
	public void setValues(StepDefinition stepDefinition) {
		if(!(stepDefinition instanceof AlfrescoReviewStepDefinition)) {
				throw new SimpleWorkflowException("An instance of AlfrescoReviewStepDefinition is required to set values");
		}
		
		AlfrescoReviewStepDefinition reviewStepDefinition = (AlfrescoReviewStepDefinition) stepDefinition;
		setName(reviewStepDefinition.getName());
		setId(reviewStepDefinition.getId());
		setDescription(reviewStepDefinition.getDescription());
		setEndProcessOnReject(reviewStepDefinition.isEndProcessOnReject());
		setAssignmentType(reviewStepDefinition.getAssignmentType());
		setRequiredApprovalCount(reviewStepDefinition.getRequiredApprovalCount());
		setAssignmentPropertyName(reviewStepDefinition.getAssignmentPropertyName());
		
		if(reviewStepDefinition.getParameters() != null) {
			setParameters(new HashMap<String, Object>(reviewStepDefinition.getParameters()));
		}
	}

	@Override
  public Map<String, Object> getParameters() {
		return parameters;
  }

	@Override
  public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
  }

	@JsonIgnore
	public List<StepDefinition> getRejectionSteps() {
		return getSteps();
  }
	
	@JsonProperty("end-process-on-reject")
	public void setEndProcessOnReject(boolean endProcessOnReject) {
	  this.endProcessOnReject = endProcessOnReject;
  }
	
	public boolean isEndProcessOnReject() {
	  return endProcessOnReject;
  }

}
