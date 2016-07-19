/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * @author Joram Barrez
 */
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
      
      // When unacquiring, we up the lock time again., so that it isn't cleared by the reset expired thread.
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
