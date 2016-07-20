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

import javax.jms.Message;
import javax.jms.TextMessage;

import org.activiti.engine.impl.asyncexecutor.ExecuteAsyncRunnable;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
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
