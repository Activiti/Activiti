package org.activiti5.engine.test.bpmn.event.timer.compatibility;
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

import java.util.Date;

import org.activiti.engine.runtime.Clock;
import org.activiti5.engine.impl.db.DbSqlSession;
import org.activiti5.engine.impl.interceptor.Command;
import org.activiti5.engine.impl.interceptor.CommandConfig;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti5.engine.impl.persistence.entity.JobEntity;
import org.activiti5.engine.impl.test.PluggableActivitiTestCase;

public abstract class TimerEventCompatibilityTest extends PluggableActivitiTestCase {

  protected void changeConfigurationToPlainText(String jobId) {

    org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5ProcessEngineConfig = (org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl)
        processEngineConfiguration.getActiviti5CompatibilityHandler().getRawProcessConfiguration();
    JobEntity job = (JobEntity) activiti5ProcessEngineConfig.getManagementService().createJobQuery().jobId(jobId).singleResult();
    
    String activityId = TimerEventHandler.getActivityIdFromConfiguration(job.getJobHandlerConfiguration());

    final JobEntity finalJob = job;
    CommandExecutor commandExecutor = (CommandExecutor) processEngineConfiguration.getActiviti5CompatibilityHandler().getRawCommandExecutor();
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
    Clock clock = processEngineConfiguration.getClock();
    Date newDate = new Date(clock.getCurrentTime().getTime() + ((minutes * 60 * 1000)));
    clock.setCurrentTime(newDate);
    processEngineConfiguration.setClock(clock);
  }
}
