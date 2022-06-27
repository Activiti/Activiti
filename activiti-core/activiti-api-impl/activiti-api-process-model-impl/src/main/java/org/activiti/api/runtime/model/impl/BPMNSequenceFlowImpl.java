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

import org.activiti.api.process.model.BPMNSequenceFlow;

public class BPMNSequenceFlowImpl extends BPMNElementImpl implements BPMNSequenceFlow {

    private String sourceActivityElementId;
    private String sourceActivityName;
    private String sourceActivityType;
    private String targetActivityElementId;
    private String targetActivityName;
    private String targetActivityType;

    public BPMNSequenceFlowImpl() {
    }

    public BPMNSequenceFlowImpl(String elementId,
                            String sourceActivityElementId,
                            String targetActivityElementId) {
        this.setElementId(elementId);
        this.sourceActivityElementId = sourceActivityElementId;
        this.targetActivityElementId = targetActivityElementId;
    }

    @Override
    public String getSourceActivityElementId() {
        return sourceActivityElementId;
    }

    @Override
    public String getSourceActivityName() {
        return sourceActivityName;
    }

    public void setSourceActivityName(String sourceActivityName) {
        this.sourceActivityName = sourceActivityName;
    }

    @Override
    public String getSourceActivityType() {
        return sourceActivityType;
    }

    public void setSourceActivityType(String sourceActivityType) {
        this.sourceActivityType = sourceActivityType;
    }

    @Override
    public String getTargetActivityElementId() {
        return targetActivityElementId;
    }

    @Override
    public String getTargetActivityName() {
        return targetActivityName;
    }

    public void setTargetActivityName(String targetActivityName) {
        this.targetActivityName = targetActivityName;
    }

    @Override
    public String getTargetActivityType() {
        return targetActivityType;
    }

    public void setTargetActivityType(String targetActivityType) {
        this.targetActivityType = targetActivityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BPMNSequenceFlowImpl that = (BPMNSequenceFlowImpl) o;
        return Objects.equals(getElementId(),
                              that.getElementId()) &&
                Objects.equals(sourceActivityElementId,
                               that.getSourceActivityElementId()) &&
                Objects.equals(sourceActivityType,
                               that.getSourceActivityType()) &&
                Objects.equals(sourceActivityName,
                               that.getSourceActivityName()) &&
                Objects.equals(targetActivityElementId,
                               that.getTargetActivityElementId()) &&
                Objects.equals(targetActivityType,
                                  that.getTargetActivityType()) &&
                Objects.equals(targetActivityName,
                                  that.getTargetActivityName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getElementId(),
                            sourceActivityElementId,
                            targetActivityElementId);
    }

    @Override
    public String toString() {
        return "SequenceFlowImpl{" +
                "sourceActivityElementId='" + sourceActivityElementId + '\'' +
                ", sourceActivityName='" + sourceActivityName + '\'' +
                ", sourceActivityType='" + sourceActivityType + '\'' +
                ", targetActivityElementId='" + targetActivityElementId + '\'' +
                ", targetActivityName='" + targetActivityName + '\'' +
                ", targetActivityType='" + targetActivityType + '\'' +
                '}';
    }
}
