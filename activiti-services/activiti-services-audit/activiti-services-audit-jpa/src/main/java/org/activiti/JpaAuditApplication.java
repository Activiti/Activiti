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

package org.activiti;

import org.activiti.services.audit.AuditConsumerChannels;
import org.activiti.services.audit.EventsRepository;
import org.activiti.services.audit.events.ProcessEngineEventEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBinding(AuditConsumerChannels.class)
public class JpaAuditApplication implements CommandLineRunner {

    private final EventsRepository eventsRepository;

    @Autowired
    public JpaAuditApplication(EventsRepository eventsRepository) {
        this.eventsRepository = eventsRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(JpaAuditApplication.class,
                              args);
    }

    @Override
    public void run(String... strings) throws Exception {
        System.out.println(">>> Starting Audit App... ");
    }

    @StreamListener(AuditConsumerChannels.AUDIT_CONSUMER)
    public synchronized void receive(ProcessEngineEventEntity event) {
        System.out.println(">>>> Recieved Event" + event);
        System.out.println(">>>> \t Event Meta Data: " + event.getTimestamp() + " > " + event.getEventType());
        System.out.println(">>>> \t Event Class: " + event.getClass().getCanonicalName());
        eventsRepository.save(event);
    }

    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }
}
