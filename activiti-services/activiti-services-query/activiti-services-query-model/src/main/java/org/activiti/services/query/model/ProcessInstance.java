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

import java.util.Date;
import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import org.springframework.format.annotation.DateTimeFormat;

@GraphQLDescription("Process Instance Entity Model")

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessInstance {

    @Id
    @GraphQLDescription("Unique process instance identity attribute")
    private String processInstanceId;
    private String processDefinitionId;
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date lastModified;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date lastModifiedTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date lastModifiedFrom;

    @GraphQLDescription("Associated tasks entities")
    @OneToMany(mappedBy="processInstance")
    private Set<Task> tasks;     

    @GraphQLDescription("Associated process instance variables")
    @OneToMany(mappedBy="processInstance")
    private Set<Variable> variables;     
    
    public ProcessInstance() {
    }

    public ProcessInstance(String processInstanceId,
                           String processDefinitionId,
                           String status,
                           Date lastModified) {
        this.processInstanceId = processInstanceId;
        this.processDefinitionId = processDefinitionId;
        this.status = status; 
        this.lastModified = lastModified;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getStatus() {
        return status;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
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

    public Set<Task> getTasks() {
        return this.tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public Set<Variable> getVariables() {
        return variables;
    }

    public void setVariables(Set<Variable> variables) {
        this.variables = variables;
    }

}