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

package org.activiti.spring.boot.process.listener;

import java.util.LinkedList;
import java.util.List;

import org.activiti.api.process.model.events.BPMNTimerFiredEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class DummyBPMNTimerFiredListener implements BPMNElementEventListener<BPMNTimerFiredEvent> {

    private final List<BPMNTimerFiredEvent> events = new LinkedList<>();

    @Override
    public synchronized void onEvent(BPMNTimerFiredEvent event) {
        events.add(event);
    }

    public synchronized List<BPMNTimerFiredEvent> getEvents() {
        return events;
    }

    public synchronized void clear(){
        events.clear();
    }
}
