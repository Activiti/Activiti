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

import java.util.Objects;

import org.activiti.api.process.model.BPMNMessage;
import org.activiti.api.process.model.payloads.MessageEventPayload;

public class BPMNMessageImpl extends BPMNElementImpl implements BPMNMessage {

    private MessageEventPayload messagePayload;

    public BPMNMessageImpl() {
    }

    public BPMNMessageImpl(String elementId) {
        this.setElementId(elementId);
    }

    public MessageEventPayload getMessagePayload() {
        return messagePayload;
    }

    public void setMessagePayload(MessageEventPayload messagePayload) {
        this.messagePayload = messagePayload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BPMNMessageImpl that = (BPMNMessageImpl) o;

        return Objects.equals(getElementId(),
                              that.getElementId()) &&
               Objects.equals(messagePayload,
                               that.getMessagePayload());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((messagePayload == null) ? 0 : messagePayload.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "BPMNMessageImpl{" +
                ", elementId='" + getElementId() + '\'' +
                ", messagePayload='" + (messagePayload != null ? messagePayload.toString() : null) + '\'' +
                '}';
    }
}
