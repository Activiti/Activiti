/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.engine.impl.interceptor;

import org.activiti.engine.impl.agenda.AbstractOperation;
import org.activiti.engine.impl.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandInvoker extends AbstractCommandInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(CommandInvoker.class);

  @Override
  @SuppressWarnings("unchecked")
  public <T> T execute(final CommandConfig config, final Command<T> command) {
    final CommandContext commandContext = Context.getCommandContext();

    // Execute the command.
    // This will produce operations that will be put on the agenda.
    commandContext.getAgenda().planOperation(new Runnable() {
      @Override
      public void run() {
        commandContext.setResult(command.execute(commandContext));
      }
    });

    // Run loop for agenda
    executeOperations(commandContext);

    // At the end, call the execution tree change listeners.
    // TODO: optimization: only do this when the tree has actually changed (ie check dbSqlSession).
    if (commandContext.hasInvolvedExecutions()) {
      Context.getAgenda().planExecuteInactiveBehaviorsOperation();
      executeOperations(commandContext);
    }

    return (T) commandContext.getResult();
  }

  protected void executeOperations(final CommandContext commandContext) {
    while (!commandContext.getAgenda().isEmpty()) {
      Runnable runnable = commandContext.getAgenda().getNextOperation();
      executeOperation(runnable);
    }
  }

  public void executeOperation(Runnable runnable) {
    if (runnable instanceof AbstractOperation) {
      AbstractOperation operation = (AbstractOperation) runnable;

      // Execute the operation if the operation has no execution (i.e. it's an operation not working on a process instance)
      // or the operation has an execution and it is not ended
      if (operation.getExecution() == null || !operation.getExecution().isEnded()) {

        if (logger.isDebugEnabled()) {
          logger.debug("Executing operation {} ", operation.getClass());
        }

        runnable.run();

      }

    } else {
      runnable.run();
    }
  }

  @Override
  public CommandInterceptor getNext() {
    return null;
  }

  @Override
  public void setNext(CommandInterceptor next) {
    throw new UnsupportedOperationException("CommandInvoker must be the last interceptor in the chain");
  }

}
