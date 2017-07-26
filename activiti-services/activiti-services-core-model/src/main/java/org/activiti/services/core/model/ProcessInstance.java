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

package org.activiti.services.core.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessInstance {

    public enum ProcessInstanceStatus {
                                       RUNNING,
                                       SUSPENDED,
                                       COMPLETED
    }

    private String id;
    private String name;
    private String description;
    private String processDefinitionId;
    private String initiator;
    private Date startDate;
    private String businessKey;
    private String status;

    public ProcessInstance() {
    }

    public ProcessInstance(String id,
                           String name,
                           String description,
                           String processDefinitionId,
                           String initiator,
                           Date startDate,
                           String businessKey,
                           String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.initiator = initiator;
        this.businessKey = businessKey;
        this.status = status;
        this.processDefinitionId = processDefinitionId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getStatus() {
        return status;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }
}