/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.api.process.model;

import java.util.Date;

import org.activiti.api.model.shared.model.ApplicationElement;

public interface ProcessInstance extends ApplicationElement {

    enum ProcessInstanceStatus {
        CREATED,
        RUNNING,
        SUSPENDED,
        CANCELLED,
        COMPLETED
    }

    String getId();

    String getName();

    Date getStartDate();

    Date getCompletedDate();

    String getInitiator();

    String getBusinessKey();

    ProcessInstanceStatus getStatus();

    String getProcessDefinitionId();

    String getProcessDefinitionKey();

    String getParentId();

    Integer getProcessDefinitionVersion();

    String getProcessDefinitionName();

}
