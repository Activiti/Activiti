package org.activiti.engine.test.logging.mdc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.logging.LogMDC;
import org.activiti.engine.test.Deployment;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import org.slf4j.LoggerFactory;

public class MDCLoggingTest extends PluggableActivitiTestCase {
  private Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

  MemoryLogAppender console = new MemoryLogAppender();

  private void setCustomLogger() {
        LoggerContext loggerContext = rootLogger.getLoggerContext();
        // we are not interested in auto-configuration
        loggerContext.reset();

    String PATTERN = "Modified Log *** ProcessDefinitionId=%X{mdcProcessDefinitionID} executionId=%X{mdcExecutionId} mdcProcessInstanceID=%X{mdcProcessInstanceID} mdcBusinessKey=%X{mdcBusinessKey} mdcTaskId=%X{mdcTaskId}  %m%n";

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

    LogMDC.setMDCEnabled(true);
  }

    private void unsetCustomLogger() {
        rootLogger.detachAppender(console);
    }

  @Deployment
  public void testLogger() {
    setCustomLogger();

    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("testLoggerProcess"));
    String messages = console.toString();

    assertThat(messages.contains("ProcessDefinitionId=" + TestService.processDefinitionId)).isTrue();
    assertThat(messages.contains("executionId=" + TestService.executionId)).isTrue();
    assertThat(messages.contains("mdcProcessInstanceID=" + TestService.processInstanceId)).isTrue();
    assertThat(messages.contains("mdcBusinessKey=" + (TestService.businessKey == null ? "" : TestService.businessKey))).isTrue();
    console.clear();
    unsetCustomLogger();

    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("testLoggerProcess"));
    assertThat(console.toString().contains("ProcessDefinitionId=" + TestService.processDefinitionId)).isFalse();
  }
}
