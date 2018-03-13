package org.activiti.engine.test.logging.mdc;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;
import org.slf4j.LoggerFactory;

public class MDCLoggingTest extends PluggableActivitiTestCase {

    private Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    MemoryLogAppender console = new MemoryLogAppender();

    private void setCustomLogger() {
        LoggerContext loggerContext = rootLogger.getLoggerContext();
        // we are not interested in auto-configuration
        loggerContext.reset();

        final String PATTERN = "Modified Log *** ProcessDefinitionId=%X{mdcProcessDefinitionID} executionId=%X{mdcExecutionId} mdcProcessInstanceID=%X{mdcProcessInstanceID} mdcBusinessKey=%X{mdcBusinessKey} mdcTaskId=%X{mdcTaskId}  %m%n";

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(PATTERN);
        encoder.start();

        console.setContext(loggerContext);
        console.setEncoder(encoder);
        console.start();

        console.setEncoder(encoder);
        console.setName("MemoryAppender");

        rootLogger.addAppender(console);
    }

    private void unsetCustomLogger() {
        rootLogger.detachAppender(console);
    }

    @Deployment
    public void testLogger() {
        setCustomLogger();

        try {
            runtimeService.startProcessInstanceByKey("testLoggerProcess");
            fail("Expected exception");
        } catch (Exception e) {
            // expected exception
        }
        String messages = console.toString();

        assertTrue(messages.contains("ProcessDefinitionId="
                + TestService.processDefinitionId));
        assertTrue(messages.contains("executionId=" + TestService.executionId));
        assertTrue(messages.contains("mdcProcessInstanceID="
                + TestService.processInstanceId));
        assertTrue(messages.contains("mdcBusinessKey="
                + (TestService.businessKey == null ? ""
                : TestService.businessKey)));

        console.clear();

        unsetCustomLogger();

        try {
            runtimeService.startProcessInstanceByKey("testLoggerProcess");
            fail("Expected exception");
        } catch (Exception e) {
            // expected exception
        }
        assertFalse(console.toString().contains(
                "ProcessDefinitionId=" + TestService.processDefinitionId));
    }
}
