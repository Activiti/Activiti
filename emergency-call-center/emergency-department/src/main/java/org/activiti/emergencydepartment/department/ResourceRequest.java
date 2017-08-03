package org.activiti.emergencydepartment.department;

public class ResourceRequest {

    private String activitiProcessInstance;
    private String note;
    private Boolean resourceConfirmed;

    public ResourceRequest(
    	String activitiProcessInstance,
    	String notes,
    	Boolean resourceConfirmed) {
		this.activitiProcessInstance = activitiProcessInstance;
		this.note = notes;
		this.resourceConfirmed = resourceConfirmed;
	}

    public String getActivitiProcessInstance() {
		return activitiProcessInstance;
	}

	public String getNotes() {
		return note;
	}

	public Boolean getResourceConfirmed() {
		return resourceConfirmed;
	}
}
