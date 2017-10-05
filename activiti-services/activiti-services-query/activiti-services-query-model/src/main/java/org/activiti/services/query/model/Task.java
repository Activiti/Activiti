/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.query.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;
import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import org.springframework.format.annotation.DateTimeFormat;

@GraphQLDescription("Task Instance Entity Model")

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String assignee;
    private String name;
    private String description;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date createTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date dueDate;
    private String priority;
    private String category;
    private String processDefinitionId;
    @Column(insertable=false, updatable=false)
    private String processInstanceId;
    private String status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date lastModified;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date lastModifiedTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date lastModifiedFrom;

    @ManyToOne
    @JoinColumn(name="processInstanceId", referencedColumnName="processInstanceId")
    private ProcessInstance processInstance;
    
    @OneToMany(fetch=FetchType.EAGER) 
    @JoinColumns({
        @JoinColumn(name="processInstanceId", referencedColumnName="processInstanceId"),
        @JoinColumn(name="taskId", referencedColumnName="id")
    })
    private Set<Variable> variables;    
    
    public Task() {
    }

    @JsonCreator
    public Task(@JsonProperty("id") String id,
                @JsonProperty("assignee") String assignee,
                @JsonProperty("name") String name,
                @JsonProperty("description") String description,
                @JsonProperty("createTime") Date createTime,
                @JsonProperty("dueDate") Date dueDate,
                @JsonProperty("priority") String priority,
                @JsonProperty("category") String category,
                @JsonProperty("processDefinitionId") String processDefinitionId,
                @JsonProperty("processInstanceId") String processInstanceId,
                @JsonProperty("status") String status,
                @JsonProperty("lastModified") Date lastModified) {
        this.id = id;
        this.assignee = assignee;
        this.name = name;
        this.description = description;
        this.createTime = createTime;
        this.dueDate = dueDate;
        this.priority = priority;
        this.category = category;
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
        this.status = status;
        this.lastModified = lastModified;
    }

    public String getId() {
        return id;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getStatus() {
        return status;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Transient
    public Date getLastModifiedTo() {
        return lastModifiedTo;
    }

    public void setLastModifiedTo(Date lastModifiedTo) {
        this.lastModifiedTo = lastModifiedTo;
    }

    @Transient
    public Date getLastModifiedFrom() {
        return lastModifiedFrom;
    }

    public void setLastModifiedFrom(Date lastModifiedFrom) {
        this.lastModifiedFrom = lastModifiedFrom;
    }

    /**
     * @return the processInstance
     */
    public ProcessInstance getProcessInstance() {
        return this.processInstance;
    }

    /**
     * @param processInstance the processInstance to set
     */
    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    /**
     * @return the variables
     */
    public Set<Variable> getVariables() {
        return this.variables;
    }

    /**
     * @param variables the variables to set
     */
    public void setVariables(Set<Variable> variables) {
        this.variables = variables;
    }

}
