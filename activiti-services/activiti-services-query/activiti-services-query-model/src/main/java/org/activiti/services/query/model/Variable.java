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

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import org.springframework.format.annotation.DateTimeFormat;

@GraphQLDescription("Variable Instance Entity Model")

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Variable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)  
    private long id;
    private String type;
    private String name;
    private String processInstanceId;
    private String taskId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date createTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date lastUpdatedTime;
    private String executionId;
    private String value;

    @ManyToOne(optional=true)
    @JoinColumn(name="taskId", referencedColumnName="id", insertable=false, updatable=false, nullable = true)
    private Task task;

    @ManyToOne
    @JoinColumn(name="processInstanceId", referencedColumnName="processInstanceId", insertable=false, updatable=false)
    private ProcessInstance processInstance;
    
    public Variable() {
    }

    public Variable(String type,
                    String name,
                    String processInstanceId,
                    String taskId,
                    Date createTime,
                    Date lastUpdatedTime,
                    String executionId,
                    String value) {
        this.type = type;
        this.name = name;
        this.processInstanceId = processInstanceId;
        this.taskId = taskId;
        this.createTime = createTime;
        this.lastUpdatedTime = lastUpdatedTime;
        this.executionId = executionId;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Date lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public ProcessInstance getProcessInstance() {
        return this.processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }
    
    public Task getTask() {
        return this.task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
    
}