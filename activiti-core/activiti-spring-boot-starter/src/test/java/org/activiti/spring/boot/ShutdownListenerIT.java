package org.activiti.spring.boot;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ShutdownListenerIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    @BeforeEach
    public void setUp() {
        processEngineConfiguration.getAsyncExecutor().start();
    }

    @Test
    public void should_shutdownAsyncExecutor_when_shutdownApplication() {
        assertThat(processEngineConfiguration.getAsyncExecutor().isActive()).isTrue();
        // Trigger the shutdown event
        applicationContext.publishEvent(new ContextClosedEvent(applicationContext));
        assertThat(processEngineConfiguration.getAsyncExecutor().isActive()).isFalse();
    }

}
