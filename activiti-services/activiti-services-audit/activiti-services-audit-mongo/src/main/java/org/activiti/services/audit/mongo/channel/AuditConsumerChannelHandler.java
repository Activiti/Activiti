/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.audit.mongo.channel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.activiti.services.audit.mongo.EventsMongoRepository;
import org.activiti.services.audit.mongo.entity.EventLogDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(AuditConsumerChannels.class)
public class AuditConsumerChannelHandler {

    private final EventsMongoRepository eventsRepository;

    @Autowired
    public AuditConsumerChannelHandler(EventsMongoRepository eventsRepository) {
        this.eventsRepository = eventsRepository;
    }

    @StreamListener(AuditConsumerChannels.AUDIT_CONSUMER)
    public synchronized void receive(String eventJsonArray) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        JsonNode jsonNode = jsonMapper.readTree(eventJsonArray);

        List<EventLogDocument> messageList = new ArrayList<>();

        ArrayNode arrayNode = (ArrayNode) jsonNode;
        for (int i = 0; i < arrayNode.size(); i++) {
            messageList.add(jsonMapper.readValue(arrayNode.get(i).toString(), EventLogDocument.class));
        }
        eventsRepository.insertAll(messageList);
    }
}