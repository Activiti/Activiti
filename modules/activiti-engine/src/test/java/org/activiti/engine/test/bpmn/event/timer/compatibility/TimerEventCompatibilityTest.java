package org.activiti.engine.test.bpmn.event.timer.compatibility;
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

import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

import java.util.Date;

public abstract class TimerEventCompatibilityTest extends PluggableActivitiTestCase {

  protected void changeConfigurationToPlainText(JobEntity job) {

    String activityId = TimerEventHandler.getActivityIdFromConfiguration(job.getJobHandlerConfiguration());

    final JobEntity finalJob = job;
    CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutor();
    CommandConfig config = new CommandConfig().transactionNotSupported();
    final String finalActivityId = activityId;
    commandExecutor.execute(config, new Command<Object>() {

      public Object execute(CommandContext commandContext) {
        DbSqlSession session = commandContext.getSession(DbSqlSession.class);
        session.delete(finalJob);
        session.flush();
        session.commit();
        return null;
      }
    });

    commandExecutor.execute(config, new Command<Object>() {

      public Object execute(CommandContext commandContext) {
        DbSqlSession session = commandContext.getSession(DbSqlSession.class);

        finalJob.setJobHandlerConfiguration(finalActivityId);
        finalJob.setId(null);
        session.insert(finalJob);

        session.flush();
        session.commit();
        return null;
      }
    });
  }

  protected void moveByMinutes(int minutes) throws Exception {
    processEngineConfiguration.getClock().setCurrentTime(new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() + ((minutes * 60 * 1000))));
  }
}
