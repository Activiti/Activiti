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

package org.activiti.engine.impl.cmd;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.MessageAddedNotification;
import org.activiti.engine.impl.runtime.JobEntity;

/**
 * @author Tom Baeyens
 */
public class DecrementJobRetriesCmd implements Command<Object> {

  protected String jobId;
  protected Throwable exception;

  public DecrementJobRetriesCmd(String jobId, Throwable exception) {
    this.jobId = jobId;
    this.exception = exception;
  }

  public Object execute(CommandContext commandContext) {
    JobEntity job = Context
      .getCommandContext()
      .getJobManager()
      .findJobById(jobId);
    job.setRetries(job.getRetries() - 1);
    job.setLockOwner(null);
    job.setLockExpirationTime(null);
    
    if(exception != null) {
      job.setExceptionMessage(exception.getMessage());
      job.setExceptionStacktrace(getExceptionStacktrace());
    }
    
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    MessageAddedNotification messageAddedNotification = new MessageAddedNotification(jobExecutor);
    TransactionContext transactionContext = commandContext.getTransactionContext();
    transactionContext.addTransactionListener(TransactionState.COMMITTED, messageAddedNotification);
    
    return null;
  }
  
  private String getExceptionStacktrace() {
    StringWriter stringWriter = new StringWriter();
    exception.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }
}
