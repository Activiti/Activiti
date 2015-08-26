/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.model.runtime;

import com.activiti.model.common.AbstractRepresentation;
import com.activiti.model.idm.LightUserRepresentation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;

import java.util.Date;
import java.util.List;

/**
 * REST representation of a task.
 * 
 * @author Tijs Rademakers
 */
public class TaskRepresentation extends AbstractRepresentation {

    protected String id;
    protected String name;
    protected String description;
    protected String category;
    protected LightUserRepresentation assignee;
    protected Date created;
    protected Date dueDate;
    protected Date endDate;
    protected Long duration;
    protected Integer priority;
    protected String processInstanceId;
    protected String processInstanceName;
    protected String processDefinitionId;
    protected String processDefinitionName;
    protected String processDefinitionDescription;
    protected String processDefinitionKey;
    protected String processDefinitionCategory;
    protected int processDefinitionVersion;
    protected String processDefinitionDeploymentId;
    protected String formKey;
    protected String processInstanceStartUserId;
    protected boolean initiatorCanCompleteTask;
    protected boolean isMemberOfCandidateGroup;
    protected boolean isMemberOfCandidateUsers;
    
    @JsonDeserialize(contentAs=LightUserRepresentation.class)
    @JsonInclude(Include.NON_NULL)
    protected List<LightUserRepresentation> involvedPeople;
    
    // Needed for serialization!
    public TaskRepresentation() {}
    
    public TaskRepresentation(Task task) {
        this(task, null);
    }
    
    public TaskRepresentation(HistoricTaskInstance task) {
    	this(task, null);
    }
    
    public TaskRepresentation(TaskInfo taskInfo, ProcessDefinition processDefinition) {
    	 this.id = taskInfo.getId();
         this.name = taskInfo.getName();
         this.description = taskInfo.getDescription();
         this.category = taskInfo.getCategory();
         this.created = taskInfo.getCreateTime();
         this.dueDate = taskInfo.getDueDate();
         this.priority = taskInfo.getPriority();
         this.processInstanceId = taskInfo.getProcessInstanceId();
         this.processDefinitionId = taskInfo.getProcessDefinitionId();
         
         if (taskInfo instanceof HistoricTaskInstance) {
        	 this.endDate = ((HistoricTaskInstance) taskInfo).getEndTime();
        	 this.formKey = taskInfo.getFormKey();
        	 this.duration = ((HistoricTaskInstance) taskInfo).getDurationInMillis();
         } else {
        	 // Rendering of forms for historic tasks not supported currently
        	 this.formKey = taskInfo.getFormKey();
         }
         
         if (processDefinition != null) {
             this.processDefinitionName = processDefinition.getName();
             this.processDefinitionDescription = processDefinition.getDescription();
             this.processDefinitionKey = processDefinition.getKey();
             this.processDefinitionCategory = processDefinition.getCategory();
             this.processDefinitionVersion = processDefinition.getVersion();
             this.processDefinitionDeploymentId = processDefinition.getDeploymentId();
         }
    }

    public TaskRepresentation(TaskInfo taskInfo, ProcessDefinition processDefinition, String processInstanceName) {
        // todo Once a ProcessInstanceInfo class is implemented, lets send inthat as the 3rd parameter istead
        this(taskInfo, processDefinition);
        this.processInstanceName = processInstanceName;
    }

    public void fillTask(Task task) {
        task.setName(name);
        task.setDescription(description);
        if (assignee != null && assignee.getId() != null) {
            task.setAssignee(String.valueOf(assignee.getId()));
        }
        task.setDueDate(dueDate);
        if (priority != null) {
            task.setPriority(priority);
        }
        task.setCategory(category);
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LightUserRepresentation getAssignee() {
        return assignee;
    }

    public void setAssignee(LightUserRepresentation assignee) {
        this.assignee = assignee;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceName() {
        return processInstanceName;
    }

    public void setProcessInstanceName(String processInstanceName) {
        this.processInstanceName = processInstanceName;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public String getProcessDefinitionDescription() {
        return processDefinitionDescription;
    }

    public void setProcessDefinitionDescription(String processDefinitionDescription) {
        this.processDefinitionDescription = processDefinitionDescription;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessDefinitionCategory() {
        return processDefinitionCategory;
    }

    public void setProcessDefinitionCategory(String processDefinitionCategory) {
        this.processDefinitionCategory = processDefinitionCategory;
    }

    public int getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(int processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    public String getProcessDefinitionDeploymentId() {
        return processDefinitionDeploymentId;
    }

    public void setProcessDefinitionDeploymentId(String processDefinitionDeploymentId) {
        this.processDefinitionDeploymentId = processDefinitionDeploymentId;
    }
    
    public String getFormKey() {
        return formKey;
    }
    
    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }
    
    public String getProcessInstanceStartUserId() {
        return processInstanceStartUserId;
    }

    public void setProcessInstanceStartUserId(String processInstanceStartUserId) {
        this.processInstanceStartUserId = processInstanceStartUserId;
    }

    public boolean isInitiatorCanCompleteTask() {
        return initiatorCanCompleteTask;
    }

    public void setInitiatorCanCompleteTask(boolean initiatorCanCompleteTask) {
        this.initiatorCanCompleteTask = initiatorCanCompleteTask;
    }

    public boolean isMemberOfCandidateGroup() {
        return isMemberOfCandidateGroup;
    }

    public void setMemberOfCandidateGroup(boolean isMemberOfCandidateGroup) {
        this.isMemberOfCandidateGroup = isMemberOfCandidateGroup;
    }

    public boolean isMemberOfCandidateUsers() {
        return isMemberOfCandidateUsers;
    }

    public void setMemberOfCandidateUsers(boolean isMemberOfCandidateUsers) {
        this.isMemberOfCandidateUsers = isMemberOfCandidateUsers;
    }

    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public List<LightUserRepresentation> getInvolvedPeople() {
        return involvedPeople;
    }
    
    public void setInvolvedPeople(List<LightUserRepresentation> involvedPeople) {
        this.involvedPeople = involvedPeople;
    }
}
