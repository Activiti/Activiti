/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.event.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.activiti.api.runtime.model.impl.MessageSubscriptionImpl;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.junit.jupiter.api.Test;

public class MessageSubscriptionConverterTest {

    private MessageSubscriptionConverter converter = new MessageSubscriptionConverter();

    @Test
    public void convertShouldReturnBPMNMessage() {

        MessageEventSubscriptionEntity entity = mock(MessageEventSubscriptionEntity.class);
        given(entity.getConfiguration()).willReturn("correlationKey");
        given(entity.getEventName()).willReturn("messageName");
        given(entity.getProcessDefinitionId()).willReturn("procDefId");
        given(entity.getProcessInstanceId()).willReturn("procInstId");

        MessageSubscriptionImpl messageSubscription = converter.convertToMessageSubscription(entity);

        //then
        assertThat(messageSubscription).isNotNull();
        assertThat(messageSubscription.getProcessInstanceId()).isEqualTo("procInstId");
        assertThat(messageSubscription.getProcessDefinitionId()).isEqualTo("procDefId");
        assertThat(messageSubscription.getConfiguration()).isEqualTo("correlationKey");
        assertThat(messageSubscription.getEventName()).isEqualTo("messageName");

    }

}
