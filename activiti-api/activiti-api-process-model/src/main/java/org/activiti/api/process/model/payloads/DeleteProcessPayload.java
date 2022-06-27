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
package org.activiti.api.process.model.payloads;

import java.util.UUID;

import org.activiti.api.model.shared.Payload;


public class DeleteProcessPayload implements Payload {

    private String id;
    private String processInstanceId;
    private String reason;

    public DeleteProcessPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public DeleteProcessPayload(String processInstanceId,
                                String reason) {
        this();
        this.processInstanceId = processInstanceId;
        this.reason = reason;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
