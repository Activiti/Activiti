/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.cfg.TransactionPropagation;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AsyncJobAddedNotification implements CommandContextCloseListener {

  private static Logger log = LoggerFactory.getLogger(AsyncJobAddedNotification.class);

  protected JobEntity job;
  protected AsyncExecutor asyncExecutor;

  public AsyncJobAddedNotification(JobEntity job, AsyncExecutor asyncExecutor) {
    this.job = job;
    this.asyncExecutor = asyncExecutor;
  }

  @Override
  public void closed(CommandContext commandContext) {
    CommandExecutor commandExecutor = commandContext.getProcessEngineConfiguration().getCommandExecutor();
    CommandConfig commandConfig = new CommandConfig(false, TransactionPropagation.REQUIRES_NEW);
    commandExecutor.execute(commandConfig, new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        if (log.isTraceEnabled()) {
          log.trace("notifying job executor of new job");
        }
        asyncExecutor.executeAsyncJob(job);
        return null;
      }
    });
  }

  @Override
  public void closing(CommandContext commandContext) {
  }

  @Override
  public void afterSessionsFlush(CommandContext commandContext) {
  }

  @Override
  public void closeFailure(CommandContext commandContext) {
  }

}
