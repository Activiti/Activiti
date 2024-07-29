/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.engine.impl.interceptor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.activiti.engine.ApplicationStatusHolder;
import org.activiti.engine.impl.agenda.DefaultActivitiEngineAgenda;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class CommandContextTest {


    @Before
    public void resetStatusToRunning() throws Exception {
        setStaticValue(ApplicationStatusHolder.class.getDeclaredField("running"), true);
    }

    @Test
    public void should_LogExceptionAtErrorLevel_when_closing() {
        ListAppender<ILoggingEvent> appender = startLogger();
        startAndCloseCommandContext(new RuntimeException());
        assertLogLevel(appender, Level.ERROR);
    }

    @Test
    public void should_LogExceptionAtWarningLevel_when_closing() {
        ListAppender<ILoggingEvent> appender = startLogger();
        ApplicationStatusHolder.shutdown();
        startAndCloseCommandContext(new RuntimeException());
        assertLogLevel(appender, Level.WARN);
    }

    @Test
    public void should_NotLogException_when_closing() {
        ListAppender<ILoggingEvent> appender = startLogger();
        startAndCloseCommandContext(null);
        Assertions.assertThat(appender.list).isEmpty();
    }

    private void startAndCloseCommandContext(Exception exception) {
        Command<?> command = (Command<Object>) commandContext -> "Hello world!";
        ProcessEngineConfigurationImpl processEngineConfiguration = new ProcessEngineConfigurationImpl() {
            @Override
            public CommandInterceptor createTransactionInterceptor() {
                return new CommandContextInterceptor();
            }
        };
        processEngineConfiguration.setEngineAgendaFactory(DefaultActivitiEngineAgenda::new);
        CommandContext commandContext = new CommandContext(command, processEngineConfiguration);
        commandContext.exception = exception;
        try {
            commandContext.close();
        } catch (Exception e) {
            //suppress exception
        }
    }

    private void assertLogLevel(ListAppender<ILoggingEvent> appender, Level level) {
        Assertions
            .assertThat(appender.list)
            .extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
            .contains(
                Tuple.tuple(
                    "Error while closing command context",
                    level
                )
            );
    }

    private ListAppender<ILoggingEvent> startLogger() {
        Logger fooLogger = (Logger) LoggerFactory.getLogger(CommandContext.class);

        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        fooLogger.addAppender(appender);
        appender.start();
        return appender;
    }

    private void setStaticValue(Field field, Object value) throws Exception {
        field.setAccessible(true);
        field.set(null, value);
    }
}
