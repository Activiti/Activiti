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
package org.activiti.impl.timer;

import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.TransactionState;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.job.TimerImpl;
import org.activiti.impl.jobexecutor.JobExecutor;
import org.activiti.impl.msg.MessageAddedNotification;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.time.Clock;


/**
 * @author Tom Baeyens
 */
public class JobExecutorTimerSession implements TimerSession, Session {

  private final CommandContext commandContext;
  private final JobExecutor jobExecutor;
  
  public JobExecutorTimerSession() {
    this.commandContext = CommandContext.getCurrent();
    this.jobExecutor = commandContext.getProcessEngineConfiguration().getJobExecutor();
  }

  public void schedule(TimerImpl timer) {
    Date duedate = timer.getDuedate();
    if (duedate==null) {
      throw new ActivitiException("duedate is null");
    }
    
    commandContext
      .getPersistenceSession()
      .insert(timer);
    
    // Check if this timer fires before the next time the job executor will check for new timers to fire.
    // This is highly unlikely because normally waitTimeInMillis is 5000 (5 seconds)
    // and timers are usually set further in the future
    int waitTimeInMillis = jobExecutor.getWaitTimeInMillis();
    if (duedate.getTime() < (Clock.getCurrentTime().getTime()+waitTimeInMillis)) {
      // then notify the job executor.
      commandContext
        .getTransactionContext()
        .addTransactionListener(TransactionState.COMMITTED, new MessageAddedNotification(jobExecutor));
    }
  }

  public void cancelTimers(ExecutionImpl execution) {
    PersistenceSession persistenceSession = commandContext.getPersistenceSession();
    List<TimerImpl> timers = persistenceSession.findTimersByExecutionId(execution.getId()); 
    for (TimerImpl timer: timers) {
      persistenceSession.delete(timer);
    }
  }

  public void close() {
  }

  public void flush() {
  }
}
