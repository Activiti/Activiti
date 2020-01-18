/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.model.impl;

import java.util.Objects;

import org.activiti.runtime.api.model.BPMNElement;

public class BPMNElementImpl implements BPMNElement {

    private String processInstanceId;
    private String processDefinitionId;

    public BPMNElementImpl() {
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BPMNElementImpl that = (BPMNElementImpl) o;
        return Objects.equals(processInstanceId,
                               that.processInstanceId) &&
                Objects.equals(processDefinitionId,
                               that.processDefinitionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(processInstanceId,
                            processDefinitionId);
    }

}
