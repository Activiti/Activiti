package org.activiti.workflow.simple.definition;

import java.util.List;

import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Reusable assignment details for a task to be performed by a user, 
 * used in for example {@link HumanStepDefinition}.
 *  
 * @author Frederik Heremans
 */
public class HumanStepAssignment {

	/**
	 * Possible types of human step assignment.
	 * 
	 * @author Frederik Heremans
	 */
	public enum HumanStepAssignmentType {
		/**
		 * The initiator is the assignee of the human step.
		 */
		INITIATOR,
		
		/**
		 * The human step is explicitally assigned to a specific user.
		 */
		USER,
		
		/**
		 * One of the specified users is a candidate for the human step.
		 */
		USERS,
		
		/**
		 * All users that are member of at least one of the specified groups, are candidate for
		 * the human step.
		 */
		GROUPS,
		
		/**
		 * Both users and group-members can be candidates for the human step.
		 */
		MIXED;
		
		
		@Override
		@JsonValue
		public String toString() {
			return name().toLowerCase();
		}
		
		@JsonCreator
		public static HumanStepAssignmentType forSimpleName(String name) {
			for(HumanStepAssignmentType type : values()) {
				if(type.toString().equals(name)) {
					return type;
				}
			}
			
			throw new SimpleWorkflowException("Invalid assignment type for human step: " + name);
		}
	}
	
	protected String assignee;
  protected List<String> candidateUsers;
  protected List<String> candidateGroups;
  protected HumanStepAssignmentType type =  HumanStepAssignmentType.INITIATOR;
  
  public HumanStepAssignmentType getType() {
	  return type;
  }
  
  /**
   * @param type the type of assignment represented. Dependeing on the type,
   * existing assignee/candidate will be removed if they are not allowed for the
   * given type. 
   */
  public void setType(HumanStepAssignmentType type) {
	  this.type = type;

	  switch (type) {
		case GROUPS:
			this.candidateUsers = null;
			this.assignee = null;
			break;

		case INITIATOR:
			this.candidateGroups = null;
			this.candidateUsers = null;
			this.assignee = null;
			break;
		case MIXED:
			this.assignee = null;
			break;
		case USER:
			this.candidateGroups = null;
			this.candidateUsers = null;
			break;
		case USERS:
			this.candidateGroups = null;
			this.assignee = null;
		}
  }
  
  @JsonInclude(Include.NON_NULL)
  public String getAssignee() {
	  return assignee;
  }
  
  public void setAssignee(String assignee) {
	  this.assignee = assignee;
	  if(assignee != null) {
	  	setType(HumanStepAssignmentType.USER);
	  }
  }
  
  @JsonInclude(Include.NON_NULL)
  public List<String> getCandidateGroups() {
	  return candidateGroups;
  }
  
  public void setCandidateGroups(List<String> candidateGroups) {
	  this.candidateGroups = candidateGroups;
	  if(candidateGroups != null && !candidateGroups.isEmpty()) {
	  	if(this.candidateUsers != null && !this.candidateUsers.isEmpty()) {
	  		setType(HumanStepAssignmentType.MIXED);
	  	} else {
	  		setType(HumanStepAssignmentType.GROUPS);
	  	}
	  }
  }
  
  @JsonInclude(Include.NON_NULL)
  public List<String> getCandidateUsers() {
	  return candidateUsers;
  }
  
  public void setCandidateUsers(List<String> candidateUsers) {
  	this.candidateUsers = candidateUsers;
  	if(candidateUsers != null && !candidateUsers.isEmpty()) {
  		if(this.candidateGroups != null && !this.candidateGroups.isEmpty()) {
  			setType(HumanStepAssignmentType.MIXED);
  		} else {
  			setType(HumanStepAssignmentType.USERS);
  		}
  	}
  }
}
