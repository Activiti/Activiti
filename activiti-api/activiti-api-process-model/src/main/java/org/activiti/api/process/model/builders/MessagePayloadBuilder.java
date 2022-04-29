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
package org.activiti.api.process.model.builders;

import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.api.process.model.payloads.StartMessagePayload;

public class MessagePayloadBuilder {

    public static StartMessagePayloadBuilder start(String name) {
        return new StartMessagePayloadBuilder().withName(name);
    }

    public static StartMessagePayloadBuilder from(StartMessagePayload startMessagePayload) {
        return StartMessagePayloadBuilder.from(startMessagePayload);
    }

    public static ReceiveMessagePayloadBuilder receive(String name) {
        return new ReceiveMessagePayloadBuilder().withName(name);
    }

    public static ReceiveMessagePayloadBuilder from(ReceiveMessagePayload receiveMessagePayload) {
        return ReceiveMessagePayloadBuilder.from(receiveMessagePayload);
    }

    public static MessageEventPayloadBuilder event(String name) {
        return new MessageEventPayloadBuilder().withName(name);
    }

    public static MessageEventPayloadBuilder from(MessageEventPayload messageEventPayload) {
        return MessageEventPayloadBuilder.from(messageEventPayload);
    }

}
