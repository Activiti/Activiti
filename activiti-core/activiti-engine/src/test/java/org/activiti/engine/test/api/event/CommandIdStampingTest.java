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

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventDispatcherImpl;
import org.activiti.engine.delegate.event.impl.ActivitiEventImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

/**
 * Unit-style tests that verify the commandId stamping logic in {@link ActivitiEventDispatcherImpl}.
 */
public class CommandIdStampingTest extends PluggableActivitiTestCase {

    private ActivitiEventDispatcherImpl dispatcher;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dispatcher = new ActivitiEventDispatcherImpl();
        var listener = new TestActivitiEventListener();
        dispatcher.addEventListener(listener);
    }

    /**
     * When a CommandContext is active, the dispatcher stamps its commandId onto events that do not yet have one.
     */
    public void testCommandIdIsStampedFromActiveCommandContext() {
        CommandContext commandContext = new CommandContext(null, processEngineConfiguration);
        Context.setCommandContext(commandContext);
        Context.setProcessEngineConfiguration(processEngineConfiguration);
        try {
            ActivitiEventImpl event = new ActivitiEventImpl(ActivitiEventType.ENTITY_CREATED);

            dispatcher.dispatchEvent(event);

            assertThat(event.getCommandId())
                .as("commandId should be stamped from the active CommandContext")
                .isNotNull()
                .isEqualTo(commandContext.getCommandId());
        } finally {
            Context.removeCommandContext();
            Context.removeProcessEngineConfiguration();
        }
    }

    /**
     * If the event already carries a commandId, the dispatcher must not overwrite it (propagated upstream ids).
     */
    public void testCommandIdIsNotOverwrittenIfAlreadySet() {
        CommandContext commandContext = new CommandContext(null, processEngineConfiguration);
        Context.setCommandContext(commandContext);
        Context.setProcessEngineConfiguration(processEngineConfiguration);
        try {
            ActivitiEventImpl event = new ActivitiEventImpl(ActivitiEventType.ENTITY_CREATED);
            String preSetId = "upstream-correlation-id";
            event.setCommandId(preSetId);

            dispatcher.dispatchEvent(event);

            assertThat(event.getCommandId())
                .as("pre-set commandId must not be overwritten by the dispatcher")
                .isEqualTo(preSetId);
        } finally {
            Context.removeCommandContext();
            Context.removeProcessEngineConfiguration();
        }
    }

    /**
     * Events fired outside a command context (e.g. during engine startup) must keep commandId as null.
     */
    public void testCommandIdIsNullWhenNoCommandContextIsActive() {
        assertThat(Context.getCommandContext()).as("no CommandContext should be active").isNull();

        ActivitiEventImpl event = new ActivitiEventImpl(ActivitiEventType.ENTITY_CREATED);

        dispatcher.dispatchEvent(event);

        assertThat(event.getCommandId()).as("commandId should remain null when no CommandContext is active").isNull();
    }
}
