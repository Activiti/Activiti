/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *
 */

package org.activiti.services.audit.mongo.events.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {


    private String id;
    private String owner;
    private String assignee;
    private String name;
    private String description;
    private Date createdDate;
    private Date claimedDate;
    private Date dueDate;
    private int priority;
    private String processDefinitionId;
    private String processInstanceId;
    private String parentTaskId;
    private String status;

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getClaimedDate() {
        return claimedDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public int getPriority() {
        return priority;
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

    public String getParentTaskId() {
        return parentTaskId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}