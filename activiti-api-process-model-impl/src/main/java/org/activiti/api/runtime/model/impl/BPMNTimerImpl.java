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

package org.activiti.api.runtime.model.impl;

import org.activiti.api.process.model.BPMNTimer;
import org.activiti.api.process.model.payloads.TimerPayload;

public class BPMNTimerImpl extends BPMNElementImpl implements BPMNTimer {

    private TimerPayload timerPayload;

    public BPMNTimerImpl() {
    }

    public BPMNTimerImpl(String elementId) {
        this.setElementId(elementId);
    }

    public TimerPayload getTimerPayload() {
        return timerPayload;
    }

    public void setTimerPayload(TimerPayload timerPayload) {
        this.timerPayload = timerPayload;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((timerPayload == null) ? 0 : timerPayload.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        BPMNTimerImpl other = (BPMNTimerImpl) obj;
        if (timerPayload == null) {
            if (other.timerPayload != null)
                return false;
        } else if (!timerPayload.equals(other.timerPayload))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BPMNActivityImpl{" +
                ", elementId='" + getElementId() + '\'' +
                ", timerPayload='" + (timerPayload != null ? timerPayload.toString() : null) + '\'' +
                '}';
    }
}
