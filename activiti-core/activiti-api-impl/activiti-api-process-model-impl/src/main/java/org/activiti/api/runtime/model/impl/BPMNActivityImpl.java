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
package org.activiti.api.runtime.model.impl;

import java.util.Objects;

import org.activiti.api.process.model.BPMNActivity;

public class BPMNActivityImpl extends BPMNElementImpl implements BPMNActivity {

    private String activityName;
    private String activityType;
    private String executionId;

    public BPMNActivityImpl() {
    }

    public BPMNActivityImpl(String elementId,
                            String activityName,
                            String activityType) {
        this.setElementId(elementId);
        this.activityName = activityName;
        this.activityType = activityType;
    }

    @Override
    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    @Override
    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    @Override
    public String getExecutionId() {
        return this.executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BPMNActivityImpl other = (BPMNActivityImpl) obj;
        return Objects.equals(activityName, other.activityName) &&
               Objects.equals(activityType, other.activityType) &&
               Objects.equals(executionId, other.executionId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(activityName, activityType, executionId);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BPMNActivityImpl [activityName=")
               .append(activityName)
               .append(", activityType=")
               .append(activityType)
               .append(", executionId=")
               .append(executionId)
               .append(", toString()=")
               .append(super.toString())
               .append("]");
        return builder.toString();
    }
}
