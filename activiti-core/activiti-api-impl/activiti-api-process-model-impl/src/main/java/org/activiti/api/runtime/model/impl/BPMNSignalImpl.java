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

import org.activiti.api.process.model.BPMNSignal;
import org.activiti.api.process.model.payloads.SignalPayload;

public class BPMNSignalImpl extends BPMNElementImpl implements BPMNSignal {

    private SignalPayload signalPayload;

    public BPMNSignalImpl() {
    }

    public BPMNSignalImpl(String elementId
    ) {
        this.setElementId(elementId);
    }

    public SignalPayload getSignalPayload() {
        return signalPayload;
    }

    public void setSignalPayload(SignalPayload signalPayload) {
        this.signalPayload = signalPayload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BPMNSignalImpl that = (BPMNSignalImpl) o;

        return Objects.equals(getElementId(),
                              that.getElementId()) &&
                Objects.equals(signalPayload,
                               that.getSignalPayload());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getElementId(),
                            signalPayload != null ? signalPayload.getId() : null,
                            signalPayload != null ? signalPayload.getName() : null);
    }

    @Override
    public String toString() {
        return "BPMNActivityImpl{" +
                ", elementId='" + getElementId() + '\'' +
                ", signalPayload='" + (signalPayload != null ? signalPayload.toString() : null) + '\'' +
                '}';
    }
}
