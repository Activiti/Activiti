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
package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.impl.cfg.MessageSession;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.runtime.MessageEntity;


/**
 * @author Tom Baeyens
 */
public class JobExecutorMessageSession implements Session, MessageSession {

  public void send(MessageEntity message) {
    CommandContext commandContext = Context.getCommandContext();
    
    commandContext
      .getDbSqlSession()
      .insert(message);
    
    
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    commandContext
      .getTransactionContext()
      .addTransactionListener(TransactionState.COMMITTED, new MessageAddedNotification(jobExecutor));
  }

  public void close() {
  }

  public void flush() {
  }
}
