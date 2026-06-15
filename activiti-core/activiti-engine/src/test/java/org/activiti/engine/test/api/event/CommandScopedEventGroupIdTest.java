/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.engine.test.api.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEventImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;

/**
 * Integration tests verifying that all {@link ActivitiEvent}s produced within the same engine command share
 * the same {@code commandId}, and that separate API calls produce distinct ids.
 */
public class CommandScopedEventGroupIdTest extends PluggableActivitiTestCase {

    private TestActivitiEventListener listener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        listener = new TestActivitiEventListener();
        processEngineConfiguration.getEventDispatcher().addEventListener(listener);
    }

    @Override
    protected void tearDown() throws Exception {
        if (listener != null) {
            processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
            listener.clearEventsReceived();
        }
        super.tearDown();
    }

    /**
     * All events emitted by a single {@code startProcessInstanceByKey} call must share one commandId.
     */
    @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
    public void testAllEventsFromSingleStartCommandShareCommandId() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        List<String> commandIds = collectCommandIds(listener.getEventsReceived());

        assertThat(commandIds)
            .as("events must carry a non-null commandId when dispatched inside a CommandContext")
            .isNotEmpty()
            .doesNotContainNull();

        Set<String> uniqueCommandIds = new HashSet<>(commandIds);
        assertThat(uniqueCommandIds)
            .as("all events from the same start command must share one commandId")
            .hasSize(1);
    }

    /**
     * Two separate API calls must produce two different commandIds.
     */
    @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
    public void testTwoSeparateStartCommandsProduceDifferentCommandIds() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");
        List<String> firstBatchIds = collectCommandIds(listener.getEventsReceived());
        listener.clearEventsReceived();

        runtimeService.startProcessInstanceByKey("oneTaskProcess");
        List<String> secondBatchIds = collectCommandIds(listener.getEventsReceived());

        assertThat(firstBatchIds).as("first batch must have commandIds").isNotEmpty().doesNotContainNull();
        assertThat(secondBatchIds).as("second batch must have commandIds").isNotEmpty().doesNotContainNull();

        String firstCommandId = firstBatchIds.getFirst();
        String secondCommandId = secondBatchIds.getFirst();
        assertThat(firstCommandId)
            .as("separate API calls must produce distinct commandIds")
            .isNotEqualTo(secondCommandId);
    }

    // -----------------------------------------------------------------------

    private List<String> collectCommandIds(List<ActivitiEvent> events) {
        List<String> ids = new ArrayList<>();
        for (ActivitiEvent event : events) {
            if (event instanceof ActivitiEventImpl) {
                ids.add(event.getCommandId());
            }
        }
        return ids;
    }
}
