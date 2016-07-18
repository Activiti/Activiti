package org.activiti.spring.executor.jms;

import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.activiti.engine.impl.asyncexecutor.DefaultJobManager;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.runtime.Job;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class MessageBasedJobManager extends DefaultJobManager {
  
  protected JmsTemplate jmsTemplate;
  
  public MessageBasedJobManager() {
    super(null);
  }

  public MessageBasedJobManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }
  
  @Override
  protected void triggerExecutorIfNeeded(final JobEntity jobEntity) {
    sendMessage(jobEntity);
  }

  @Override
  public void unacquire(final Job job) {
    
    if (job instanceof JobEntity) {
      JobEntity jobEntity = (JobEntity) job;
      jobEntity.setLockExpirationTime(new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() 
          + processEngineConfiguration.getAsyncExecutor().getAsyncJobLockTimeInMillis()));
    }
    
    sendMessage(job);
  }
  
  protected void sendMessage(final Job jobEntity) {
    Context.getTransactionContext().addTransactionListener(TransactionState.COMMITTED, new TransactionListener() {
      public void execute(CommandContext commandContext) {
        jmsTemplate.send(new MessageCreator() {
          public Message createMessage(Session session) throws JMSException {
            return session.createTextMessage(jobEntity.getId());
          }
        });
      }
    });
  }
  
  public JmsTemplate getJmsTemplate() {
    return jmsTemplate;
  }

  public void setJmsTemplate(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

}
