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
package org.activiti.engine.impl.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class ByteArrayRefTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger byteArrayRefLogger;

    private CommandContext commandContext;
    private ByteArrayEntityManager byteArrayEntityManager;

    @BeforeEach
    void setUp() {
        byteArrayRefLogger = (Logger) LoggerFactory.getLogger(ByteArrayRef.class);
        appender = new ListAppender<>();
        byteArrayRefLogger.addAppender(appender);
        appender.start();

        commandContext = mock(CommandContext.class);
        byteArrayEntityManager = mock(ByteArrayEntityManager.class);
        when(commandContext.getByteArrayEntityManager()).thenReturn(byteArrayEntityManager);
        Context.setCommandContext(commandContext);
    }

    @AfterEach
    void tearDown() {
        byteArrayRefLogger.detachAppender(appender);
        Context.removeCommandContext();
    }

    @Test
    void setValue_should_logDebug_when_insertingNewByteArrayEntity() {
        ByteArrayEntity entity = mock(ByteArrayEntity.class);
        when(byteArrayEntityManager.create()).thenReturn(entity);
        when(entity.getId()).thenReturn("newId");

        ByteArrayRef byteArrayRef = new ByteArrayRef();
        byteArrayRef.setValue("myVar", new byte[] { 1, 2, 3 });

        assertThat(appender.list)
            .filteredOn(e -> e.getLevel() == Level.DEBUG)
            .extracting(ILoggingEvent::getFormattedMessage)
            .anyMatch(msg -> msg.contains("myVar") && msg.contains("newId") && msg.contains("3 bytes"));
    }

    @Test
    void setValue_should_logDebug_when_updatingExistingByteArrayEntity() {
        ByteArrayEntity entity = mock(ByteArrayEntity.class);
        when(byteArrayEntityManager.findById(any())).thenReturn(entity);
        when(entity.getName()).thenReturn("myVar");

        ByteArrayRef byteArrayRef = new ByteArrayRef("existingId");
        byteArrayRef.setValue("myVar", new byte[] { 1, 2, 3, 4 });

        assertThat(appender.list)
            .filteredOn(e -> e.getLevel() == Level.DEBUG)
            .extracting(ILoggingEvent::getFormattedMessage)
            .anyMatch(msg -> msg.contains("myVar") && msg.contains("existingId") && msg.contains("4 bytes"));
    }

    @Test
    void setValue_should_notCreateEntity_when_bytesAreNull() {
        ByteArrayRef byteArrayRef = new ByteArrayRef();
        byteArrayRef.setValue("myVar", null);

        assertThat(appender.list).isEmpty();
    }
}
