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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class VariableScopeImplTest {

    private static final int FIVE_MB = 5 * 1024 * 1024;

    private ListAppender<ILoggingEvent> appender;
    private Logger variableScopeLogger;

    private final VariableScopeImpl scope = new VariableScopeImpl() {
        @Override
        protected Collection<VariableInstanceEntity> loadVariableInstances() {
            return List.of();
        }

        @Override
        protected VariableScopeImpl getParentVariableScope() {
            return null;
        }

        @Override
        protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {}

        @Override
        protected VariableInstanceEntity getSpecificVariable(String variableName) {
            return null;
        }

        @Override
        protected List<VariableInstanceEntity> getSpecificVariables(Collection<String> variableNames) {
            return List.of();
        }
    };

    @BeforeEach
    void setUp() {
        variableScopeLogger = (Logger) LoggerFactory.getLogger(VariableScopeImpl.class);
        appender = new ListAppender<>();
        variableScopeLogger.addAppender(appender);
        appender.start();
    }

    @AfterEach
    void tearDown() {
        variableScopeLogger.detachAppender(appender);
    }

    @Test
    void logLargeVariableWarning_should_logWarning_when_variableExceedsFiveMb() {
        VariableInstanceEntity variableInstance = mock(VariableInstanceEntity.class);
        when(variableInstance.getName()).thenReturn("bigVar");
        when(variableInstance.getBytes()).thenReturn(new byte[FIVE_MB + 1]);

        scope.logLargeVariableWarning(variableInstance);

        assertThat(appender.list)
            .filteredOn(e -> e.getLevel() == Level.WARN)
            .extracting(ILoggingEvent::getFormattedMessage)
            .anyMatch(msg -> msg.contains("bigVar") && msg.contains("5MB"));
    }

    @Test
    void logLargeVariableWarning_should_notLog_when_variableIsExactlyFiveMb() {
        VariableInstanceEntity variableInstance = mock(VariableInstanceEntity.class);
        when(variableInstance.getBytes()).thenReturn(new byte[FIVE_MB]);

        scope.logLargeVariableWarning(variableInstance);

        assertThat(appender.list)
            .filteredOn(e -> e.getLevel() == Level.WARN)
            .isEmpty();
    }

    @Test
    void logLargeVariableWarning_should_notLog_when_variableHasNoBytes() {
        VariableInstanceEntity variableInstance = mock(VariableInstanceEntity.class);
        when(variableInstance.getBytes()).thenReturn(null);

        scope.logLargeVariableWarning(variableInstance);

        assertThat(appender.list)
            .filteredOn(e -> e.getLevel() == Level.WARN)
            .isEmpty();
    }
}
