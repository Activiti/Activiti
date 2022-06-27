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

import org.activiti.api.process.model.BPMNError;

public class BPMNErrorImpl extends BPMNActivityImpl implements BPMNError {

    private String errorCode;
    private String errorId;

    public BPMNErrorImpl() {
    }

    public BPMNErrorImpl(String elementId) {
        this.setElementId(elementId);
    }
    public BPMNErrorImpl(String elementId,
                         String activityName,
                         String activityType) {
        this.setElementId(elementId);
        this.setActivityName(activityName);
        this.setActivityType(activityType);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getElementId(),
                            getActivityName(),
                            getActivityType(),
                            getErrorId(),
                            getErrorCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BPMNErrorImpl that = (BPMNErrorImpl) o;

        return Objects.equals(getElementId(),
                              that.getElementId()) &&
               Objects.equals(getActivityName(),
                              that.getActivityName()) &&
               Objects.equals(getActivityType(),
                              that.getActivityType()) &&
               Objects.equals(getErrorCode(),
                              that.getErrorCode()) &&
               Objects.equals(getErrorId(),
                              that.getErrorId());
    }

    @Override
    public String toString() {
        return "BPMNActivityImpl{" +
                "activityName='" + getActivityName() + '\'' +
                ", activityType='" + getActivityType() + '\'' +
                ", elementId='" + getElementId() + '\'' +
                ", errorId='" + getErrorId() + '\'' +
                ", errorCode='" + getErrorCode() + '\'' +
                '}';
    }

}
