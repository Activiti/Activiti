package org.activiti.spring.executor.jms;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.activiti.engine.impl.asyncexecutor.ExecuteAsyncRunnable;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobMessageListener implements javax.jms.MessageListener {
  
  private static final Logger logger = LoggerFactory.getLogger(JobMessageListener.class);
  
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public void onMessage(final Message message) {
    try {
      if (message instanceof TextMessage) {
        TextMessage textMessage = (TextMessage) message;
        String jobId = textMessage.getText();
        
        ExecuteAsyncRunnable executeAsyncRunnable = new ExecuteAsyncRunnable(jobId, processEngineConfiguration);
        executeAsyncRunnable.run();
        
      }
    } catch (Exception e) {
      logger.error("Exception when handling message from job queue", e);
    }
  }

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }
  
}
